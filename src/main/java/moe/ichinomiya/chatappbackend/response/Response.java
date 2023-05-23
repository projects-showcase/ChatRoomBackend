package moe.ichinomiya.chatappbackend.response;

import lombok.Data;
import lombok.ToString;
import moe.ichinomiya.chatappbackend.clientdata.ServerPacketTypes;

@Data
@ToString
public class Response {
    private boolean success;
    private String type;
    private String message;
    private Object data;

    public Response(boolean success) {
        this.success = success;
    }

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Response(boolean success, ServerPacketTypes type, String message, Object data) {
        this.success = success;
        this.type = type.getName();
        this.message = message;
        this.data = data;
    }
}
