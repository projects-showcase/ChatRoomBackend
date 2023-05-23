package moe.ichinomiya.chatappbackend.api;

import jakarta.servlet.http.HttpServletResponse;
import moe.ichinomiya.chatappbackend.clientdata.ServerPacketTypes;
import moe.ichinomiya.chatappbackend.model.User;
import moe.ichinomiya.chatappbackend.response.Response;
import moe.ichinomiya.chatappbackend.response.responses.UserData;
import moe.ichinomiya.chatappbackend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/user")
public class UserAPI {
    private static final Logger logger = LogManager.getLogger(UserAPI.class);
    UserService userService;

    public UserAPI(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("login")
    public Response login(String username, String password) {
        String token = userService.login(username, password);
        return new Response(true, "登录成功", token);
    }

    @PostMapping("register")
    public Response register(String username, String password) {
        userService.register(username, password);
        return new Response(true, "注册成功");
    }

    @PostMapping("updatePassword")
    public Response updatePassword(String token, String newPassword) {
        userService.updatePassword(token, newPassword);
        return new Response(true, "修改密码成功");
    }


    @PostMapping("updateAvatar")
    public Response handleFileUpload(String token, @RequestParam("file") MultipartFile file) {
        User user = userService.getUserByToken(token);

        // 处理文件上传逻辑
        if (!file.isEmpty()) {
            try {
                // 新建Avatar文件夹
                File avatarDir = new File("avatar");
                if (!avatarDir.exists()) {
                    if (!avatarDir.mkdir()) {
                        return new Response(false, "文件上传失败");
                    }
                }

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || (!originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".png"))) {
                    return new Response(false, "不支持的文件类型");
                }

                // 写入文件
                File avatar = new File("avatar/" + user.getId() + originalFilename.substring(originalFilename.lastIndexOf('.')));
                if (avatar.exists()) {
                    if (!avatar.delete()) {
                        logger.info("文件删除失败");
                        return new Response(false, "文件上传失败");
                    }
                }

                logger.info("Ready to write: " + avatar.getAbsolutePath());
                if (avatar.createNewFile()) {
                    file.transferTo(avatar.getAbsoluteFile());
                    WebSocket.broadcastResponse(new Response(true, ServerPacketTypes.UserProfile, "有用户修改昵称", new UserData(user.getId(), user.getUsername(), user.getNickName(), false)));
                    WebSocket.sendResponse(user.getId(), new Response(true, ServerPacketTypes.UserProfile, "修改昵称成功", new UserData(user.getId(), user.getUsername(), user.getNickName(), true)));
                    return new Response(true, "文件上传成功");
                }

                return new Response(false, "文件上传失败");
            } catch (IOException e) {
                e.printStackTrace();
                // 返回错误消息或其他逻辑
                return new Response(false, "文件上传失败");
            }
        } else {
            // 文件为空，返回错误消息或其他逻辑
            return new Response(false, "文件上传失败");
        }
    }

    File defaultAvatar = new File("avatar/default.png");

    @RequestMapping("getAvatar")
    public String fileDownLoad(HttpServletResponse response, String token, long uid) {
        User user = userService.getUserByToken(token);
        logger.info("{} queried avatar of {}", user.getUsername(), uid);

        File pngAvatar = new File("avatar/" + uid + ".png");
        File jpgAvatar = new File("avatar/" + uid + ".jpg");

        File avatar;
        if (pngAvatar.exists()) {
            avatar = pngAvatar;
        } else if (jpgAvatar.exists()) {
            avatar = jpgAvatar;
        } else {
            avatar = defaultAvatar;
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) avatar.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + avatar.getName());

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(avatar));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            logger.error(e);
            return "下载失败";
        }
        return "下载成功";
    }
}
