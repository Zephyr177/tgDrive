package com.skydevs.tgdrive.service;

import com.pengrad.telegrambot.model.File;
import com.skydevs.tgdrive.dto.UploadFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

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
     * 上传文件
     * @author SkyDev
     * @param multipartFile 文件
     * @param request 上传文件请求
     * @date 2025-07-16 16:33:39
     */
    UploadFile getUploadFile(MultipartFile multipartFile, HttpServletRequest request);

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

    /**
     * Description:
     * 根据ID获取文件名
     * @author SkyDev
     * @param fileID 文件id
     * @date 2025-07-16 16:34:44
     */
    String getFileNameByID(String fileID);

    /**
     * Description:
     * 上传文件到Telegram
     * @author SkyDev
     * @param inputStream 文件输入流
     * @param path 文件路径
     * @return 文件ID
     * @date 2025-07-16 16:39:22
     */
    String uploadFile(InputStream inputStream, String path);

    /**
     * Description:
     * WebDAV上传
     * @author SkyDev
     * @param inputStream 上传流
     * @param path 路径
     * @param request WebDAV请求
     * @return fileID
     * @date 2025-07-16 16:39:33
     */
    String uploadFile(InputStream inputStream, String path, HttpServletRequest request);

    /**
     * Description:
     * 从Telegram下载文件
     * @author SkyDev
     * @param fileId 文件ID
     * @return 文件输入流
     * @date 2025-07-16 16:39:44
     */
    InputStream downloadFile(String fileId);

    /**
     * Description:
     * 从Telegram删除文件
     * @author SkyDev
     * @param fileId 文件ID
     * @date 2025-07-16 16:39:44
     */
    void deleteFile(String fileId);
}
