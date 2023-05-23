package moe.ichinomiya.chatappbackend.clientdata;

import jakarta.websocket.Session;
import lombok.Data;
import lombok.ToString;
import moe.ichinomiya.chatappbackend.model.User;
import moe.ichinomiya.chatappbackend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;

@ToString
@Data
public class ClientData {
    private static final Logger logger = LogManager.getLogger(ClientData.class);
    // 使用HashMap加速从String类型Type到DataTypes的转换
    private static final HashMap<String, ClientPacketTypes> types = new HashMap<>();

    static {
        for (ClientPacketTypes type : ClientPacketTypes.values()) {
            types.put(type.getName(), type);
            logger.info("Registered type: " + type.getName());
        }
    }

    private String type;
    private LinkedHashMap<String, Object> data;

    public void process(User user, Session session, UserService userService) throws Exception {
        ClientPacketTypes type = types.get(this.type);

        if (type == null) {
            throw new RuntimeException("未知的数据类型");
        }

        type.execute(data, user, session, userService);
    }
}
