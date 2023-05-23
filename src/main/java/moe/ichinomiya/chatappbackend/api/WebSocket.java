package moe.ichinomiya.chatappbackend.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import moe.ichinomiya.chatappbackend.model.User;
import moe.ichinomiya.chatappbackend.clientdata.ClientData;
import moe.ichinomiya.chatappbackend.response.Response;
import moe.ichinomiya.chatappbackend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;

@ServerEndpoint("/ws/{token}")
@Component
@Controller
public class WebSocket {
    private static final Logger logger = LogManager.getLogger(WebSocket.class);
    private static final HashMap<Integer, Session> sessionMap = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        WebSocket.userService = userService;
    }

    // Send message to remote session
    public static void sendResponse(Session session, Response response) throws IOException {
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(response));
    }

    // Send message with user uid
    public static void sendResponse(int uid, Response response) throws IOException {
        Session session = sessionMap.get(uid);
        if (session != null) {
            sendResponse(session, response);
        }
    }

    // Broadcast Message
    public static void broadcastResponse(Response response) throws IOException {
        for (Session session : sessionMap.values()) {
            sendResponse(session, response);
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "token") String token) throws IOException {
        try {
            User user = userService.getUserByToken(token);

            if (sessionMap.containsKey(user.getId())) {
                Session oldSession = sessionMap.get(user.getId());
                if (oldSession.isOpen()) {
                    logger.info("用户 " + user.getUsername() + " 已在其他地方登录");
                    sendResponse(oldSession, new Response(false, "您的账号在其他地方登录，您已被迫下线"));
                    oldSession.close();
                }
            }

            logger.info("用户 " + user.getUsername() + " 已连接");
            sessionMap.put(user.getId(), session);
            sendResponse(session, new Response(true, "登录成功"));
        } catch (RuntimeException e) {
            sendResponse(session, new Response(false, "登录状态已失效，请重新登录"));
            session.close();
        }
    }

    @OnClose
    public void onClose(@PathParam(value = "token") String token) {
        User user = userService.getUserByToken(token);
        logger.info("用户 " + user.getUsername() + " 已断开连接");
        sessionMap.remove(user.getId());
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam(value = "token") String token) throws Exception {
        User user = userService.getUserByToken(token);
        ClientData clientData = objectMapper.readValue(message, ClientData.class);
        try {
            clientData.process(user, session, userService);
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendResponse(session, new Response(false, e.getMessage()));
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket 发生错误", throwable);;
    }
}
