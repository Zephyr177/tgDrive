package com.skydevs.tgdrive.mapper;

import com.skydevs.tgdrive.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Description:
 * 用户Mapper
 * @author SkyDev
 * @date 2025-07-14 09:50:39
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return user
     * @author SkyDev
     * @date 2025-07-14 09:50:41
     */
    @Select("SELECT * FROM users where username = #{username}")
    User getUserByUsername(String username);

    /**
     * Description:
     * 根据id查询用户
     * @param id 用户id
     * @return User
     * @author SkyDev
     * @date 2025-07-14 09:51:55
     */
    @Select("SELECT * FROM users where id = #{id}")
    User getUserById(long id);

    /**
     * Description:
     * 根据用户id更新密码
     * @param id 用户id
     * @param newPassword
     * @author SkyDev
     * @date 2025-07-14 09:52:42
     */
    @Update("UPDATE users SET password = #{newPassword} where id = #{id}")
    void updatePassword(long id, String newPassword);
}
