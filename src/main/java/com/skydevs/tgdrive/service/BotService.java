package com.skydevs.tgdrive.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;

/**
 * 机器人服务类，用于初始化机器人和运行机器人相关服务
 */
public interface BotService{
    /**
     * Description:
     * 获取bot token
     * @author SkyDev
     * @date 2025-07-16 16:32:53
     */
    String getBotToken();

    /**
     * Description:
     * 获得bot
     * @author SkyDev
     * @date 2025-07-17 11:46:58
     */
    TelegramBot getBot();

    /**
     * Description:
     * 获取chatID
     * @author SkyDev
     * @date 2025-07-17 11:49:29
     */
    String getChatId();

    /**
     * Description:
     * 根据文件名设置botToken
     * @author SkyDev
     * @param filename 配置文件名
     * @date 2025-07-16 16:33:14
     */
    void setBotToken(String filename);

    /**
     * Description:
     * 发送消息
     * @author SkyDev
     * @param message 要发送的消息
     * @date 2025-07-16 16:33:26
     */
    boolean sendMessage(String message);

    /**
     * Description:
     * 获取完整下载路径
     * @author SkyDev
     * @param file 文件
     * @date 2025-07-16 16:34:05
     */
    String getFullDownloadPath(File file);


    /**
     * Description:
     * 根据fileId获取文件
     * @author SkyDev
     * @param fileId 文件id
     * @date 2025-07-16 16:34:27
     */
    File getFile(String fileId);
}
