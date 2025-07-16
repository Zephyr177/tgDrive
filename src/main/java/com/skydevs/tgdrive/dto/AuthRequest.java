package com.skydevs.tgdrive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 授权请求DTO
 * @author SkyDev
 * @date 2025-07-14 08:53:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
    private String username;
    private String password;
}
