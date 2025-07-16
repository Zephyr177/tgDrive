package com.skydevs.tgdrive.service;

import org.springframework.web.multipart.MultipartFile;

public interface BackupService {
    /**
     * Description:
     * 加载备份数据库
     * @author SkyDev
     * @date 2025-07-16 16:31:34
     */
    void loadBackupDb(MultipartFile db) throws Exception;
}
