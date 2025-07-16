package com.skydevs.tgdrive.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 用户信息
 * @author SkyDev
 * @date 2025-07-14 09:33:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;

    private String username;

    private String password;

    /**
     * 权限等级
     */
    private String role;
}
