package moe.ichinomiya.chatappbackend.response.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageData {
    String id;
    int senderId;
    String message;
}
