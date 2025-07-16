package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.dto.AuthRequest;
import com.skydevs.tgdrive.dto.ChangePasswordRequest;
import com.skydevs.tgdrive.entity.User;

public interface UserService {

    /**
     * Description:
     * 用户登入
     * @author SkyDev
     * @param authRequest 请求参数（用户名，密码）
     * @return User
     */
    User login(AuthRequest authRequest);

    /**
     * Description:
     * 修改密码
     * @author SkyDev
     * @param changePasswordRequest 新老密码
     */
    void changePassword(ChangePasswordRequest changePasswordRequest);
}
