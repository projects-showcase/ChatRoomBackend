package moe.ichinomiya.chatappbackend.service;

import moe.ichinomiya.chatappbackend.mapper.UserMapper;
import moe.ichinomiya.chatappbackend.model.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // Get user by Token
    public User getUserByToken(String token) {
        User user = userMapper.selectFirstByToken(token);

        if (user == null) {
            throw new RuntimeException("无此用户");
        }

        return user;
    }

    // Get user by Username
    public User getUserByUsername(String username) {
        User user = userMapper.selectFirstByUsername(username);
        if (user == null) {
            throw new RuntimeException("无此用户");
        }
        return user;
    }

    // Get user by userId
    public User getUserByUserId(int userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("无此用户");
        }
        return user;
    }

    // Check if the username is already registered
    public boolean isUsernameRegistered(String username) {
        return userMapper.selectFirstByUsername(username) != null;
    }

    // Register a new user
    public void register(String username, String password) {
        if (isUsernameRegistered(username)) {
            throw new RuntimeException("用户名已被注册");
        }

        User user = new User();
        user.setUsername(username);
        String salt = RandomStringUtils.randomAlphanumeric(16);
        String hashedPassword = DigestUtils.sha256Hex(password + salt);

        user.setSalt(salt);
        user.setPassword(hashedPassword);
        user.setNickName(username);

        userMapper.insert(user);
    }

    // Update password
    public void updatePassword(String token, String password) {
        User user = getUserByToken(token);

        String salt = RandomStringUtils.randomAlphanumeric(16);
        String hashedPassword = DigestUtils.sha256Hex(password + salt);

        userMapper.updatePasswordAndSaltById(hashedPassword, salt, user.getId());
    }

    // Update nickname
    public void updateNickname(String token, String nickname) {
        User user = getUserByToken(token);
        userMapper.updateNicknameById(nickname, user.getId());
    }

    // Login
    public String login(String username, String password) {
        User user = getUserByUsername(username);

        if (user.getPassword() != null && !isPasswordCorrect(password, user.getPassword(), user.getSalt())) {
            throw new RuntimeException("密码错误");
        }

        String token = generateToken();

        userMapper.updateTokenById(token, user.getId());
        return token;
    }

    public String generateToken() {
        String token;
        do {
            token = RandomStringUtils.randomAlphanumeric(128);
        } while (userMapper.selectFirstByToken(token) != null);

        return token;
    }

    private boolean isPasswordCorrect(String password, String hashedPassword, String salt) {
        return DigestUtils.sha256Hex(password + salt).equals(hashedPassword);
    }
}
