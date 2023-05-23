package moe.ichinomiya.chatappbackend.clientdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.Getter;
import moe.ichinomiya.chatappbackend.api.WebSocket;
import moe.ichinomiya.chatappbackend.model.User;
import moe.ichinomiya.chatappbackend.response.Response;
import moe.ichinomiya.chatappbackend.response.responses.MessageData;
import moe.ichinomiya.chatappbackend.response.responses.UserData;
import moe.ichinomiya.chatappbackend.service.UserService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

public enum ClientPacketTypes {
    SendMessage("SEND_MESSAGE") {
        @Override
        public void execute(LinkedHashMap<String, Object> data, User user, Session session, UserService userService) throws Exception {
            sendResponse(session, new Response(true, ServerPacketTypes.Notice, "发送消息成功", null));
            WebSocket.broadcastResponse(new Response(true, ServerPacketTypes.Message, null, new MessageData(UUID.randomUUID().toString(), user.getId(), (String) data.get("message"))));
        }
    },
    ChangeNickName("CHANGE_NICK_NAME") {
        @Override
        public void execute(LinkedHashMap<String, Object> data, User user, Session session, UserService userService) throws Exception {
            String nickName = (String) data.get("nickName");
            userService.updateNickname(user.getToken(), nickName);
            WebSocket.broadcastResponse(new Response(true, ServerPacketTypes.UserProfile, "有用户修改昵称", new UserData(user.getId(), user.getUsername(), nickName, false)));
            sendResponse(session, new Response(true, ServerPacketTypes.UserProfile, "修改昵称成功", new UserData(user.getId(), user.getUsername(), nickName, true)));
        }
    },
    GetUserProfile("GET_USER_PROFILE") {
        @Override
        public void execute(LinkedHashMap<String, Object> data, User user, Session session, UserService userService) throws Exception {
            int userId = (Integer) data.get("userId");

            User targetUser = userService.getUserByUserId(userId);
            sendResponse(session, new Response(true, ServerPacketTypes.UserProfile, "获取用户资料成功", new UserData(targetUser.getId(), targetUser.getUsername(), targetUser.getNickName(), user.getId() == userId)));
        }
    },
    RecallMessage("RECALL_MESSAGE") {
        @Override
        public void execute(LinkedHashMap<String, Object> data, User user, Session session, UserService userService) throws Exception {
            String id = (String) data.get("id");
            WebSocket.broadcastResponse(new Response(true, ServerPacketTypes.RecallMessage, null, id));
        }
    };

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private final String name;

    ClientPacketTypes(String name) {
        this.name = name;
    }

    // Send message to remote session
    public static void sendResponse(Session session, Response response) throws IOException {
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(response));
    }

    public abstract void execute(LinkedHashMap<String, Object> data, User user, Session session, UserService userService) throws Exception;
}
