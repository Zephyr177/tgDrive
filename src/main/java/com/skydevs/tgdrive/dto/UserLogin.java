package com.skydevs.tgdrive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 用户登入DTO
 * @author SkyDev
 * @date 2025-07-14 08:56:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogin {
    /**
     * JWT token
     */
    private String token;

    private Long UserId;

    /**
     * 权限等级
     */
    private String role;
}
