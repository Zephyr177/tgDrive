package com.skydevs.tgdrive.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.skydevs.tgdrive.dto.AuthRequest;
import com.skydevs.tgdrive.dto.ChangePasswordRequest;
import com.skydevs.tgdrive.dto.UserLogin;
import com.skydevs.tgdrive.entity.User;
import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * 用户控制类
 * @author SkyDev
 * @date 2025-07-11 17:52:21
 */
@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Description:
     * 登入
     * @param authRequest 用户名、密码
     * @author SkyDev
     * @date 2025-07-11 17:52:44
     */
    @PostMapping("/login")
    public Result<UserLogin> login(@RequestBody AuthRequest authRequest) {
        // 验证用户名和密码
        User user = userService.login(authRequest);

        StpUtil.login(user.getId());
        UserLogin userLogin = UserLogin.builder()
                .UserId(user.getId())
                .token(StpUtil.getTokenValue())
                .role(user.getRole())
                .build();

        log.info(user.getId() + "登入");
        return Result.success(userLogin);
    }

    /**
     * Description:
     * 修改密码
     * @param changePasswordRequest 修改密码请求
     * @author SkyDev
     * @date 2025-07-11 17:53:25
     */
    @PostMapping("change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        long userId = StpUtil.getLoginIdAsLong();

        userService.changePassword(changePasswordRequest);

        log.info(userId + "密码修改" + changePasswordRequest.getUsername() + "成功");
        return Result.success("密码修改成功");
    }
}
