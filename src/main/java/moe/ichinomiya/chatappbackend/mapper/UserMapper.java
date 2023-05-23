package moe.ichinomiya.chatappbackend.mapper;

import moe.ichinomiya.chatappbackend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectFirstByUsername(@Param("username") String username);

    User selectFirstByToken(@Param("token") String token);

    int updateNicknameById(@Param("updatedNickName") String updatedNickName, @Param("id") Integer id);

    int updatePasswordAndSaltById(@Param("updatedPassword") String updatedPassword, @Param("updatedSalt") String updatedSalt, @Param("id") Integer id);

    int updateTokenById(@Param("updatedToken")String updatedToken,@Param("id")Integer id);


}