package com.skydevs.tgdrive.dto;

import lombok.*;

/**
 * Description:
 * 机器人配置DTO
 * @author SkyDev
 * @date 2025-07-14 08:54:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigForm {
    /**
     * 配置文件名
     */
    private String name;

    /**
     * telegram bot token
     */
    private String token;

    /**
     * 目标：群组或用户
     */
    private String target;

    @SuppressWarnings("unused")
    private String pass;

    private String url;
}
