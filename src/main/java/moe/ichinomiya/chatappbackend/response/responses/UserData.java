package moe.ichinomiya.chatappbackend.response.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserData {
    int uid;
    String username;
    String nickname;
    boolean isLocal;
}
