package com.skydevs.tgdrive.dto;

import lombok.*;

/**
 * Description:
 * 上传文件DTO
 * @author SkyDev
 * @date 2025-07-14 08:55:32
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UploadFile {
    private String fileName;
    private String downloadLink;
}
