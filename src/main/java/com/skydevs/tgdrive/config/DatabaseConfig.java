package com.skydevs.tgdrive.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Description:
 * 数据库配置
 * @author SkyDev
 * @date 2025-07-11 17:22:34
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    /**
     * Description:
     * 数据库初始化
     * @author SkyDev
     * @date 2025-07-11 17:22:48
     */
    @PostConstruct
    public void init() {
        // 从数据库URL中提取路径
        String dbPath = databaseUrl.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // 自动创建父目录
        }
    }
}
