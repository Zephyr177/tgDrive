package com.skydevs.tgdrive.entity;

import lombok.*;

/**
 * Description:
 * 文件信息
 * @author SkyDev
 * @date 2025-07-11 17:42:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {
    private String fileName;

    private String downloadUrl;

    /**
     * 文件大小（只保留2位小数，当时脑子抽了，别骂了别骂了）
     */
    private String size;

    /**
     * 文件大小
     */
    private Long fullSize;

    /**
     * 文件唯一id
     */
    private String fileId;

    /**
     * 用于存储上传时间的 UNIX 时间戳
     */
    private Long uploadTime;

    /**
     * WebDAV文件路径
     */
    private String webdavPath;

    /**
     * 判断是否是文件夹
     */
    private boolean dir;
}

