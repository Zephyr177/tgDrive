package com.skydevs.tgdrive.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 更改密码请求DTO
 * @author SkyDev
 * @date 2025-07-14 08:54:21
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChangePasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}
