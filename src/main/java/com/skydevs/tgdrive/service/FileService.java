package com.skydevs.tgdrive.service;

import com.pengrad.telegrambot.TelegramBot;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.result.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

public interface FileService {

    /**
     * Description:
     * 获取文件分页
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @return FileInfos
     * @author SkyDev
     * @date 2025-07-14 10:14:57
     */
    PageResult<FileInfo> getFileList(int pageNum, int pageSize);

    /**
     * Description:
     * 更新文件url
     * @param request 请求
     * @author SkyDev
     * @date 2025-07-14 10:16:51
     */
    void updateUrl(HttpServletRequest request);

    /**
     * Description:
     * 上传文件
     * @author SkyDev
     * @param multipartFile 文件
     * @param request 上传文件请求
     * @date 2025-07-16 16:33:39
     */
    UploadFile getUploadFile(MultipartFile multipartFile, HttpServletRequest request, String chatId, TelegramBot bot);

    /**
     * Description:
     * 从Telegram删除文件
     * @author SkyDev
     * @param fileId 文件ID
     * @date 2025-07-16 16:39:44
     */
    void deleteFile(String fileId);

    /**
     * Description:
     * 通过webdav上传文件
     * @param inputStream 文件输入流
     * @param request 上传文件请求
     * @return fileId
     * @author SkyDev
     * @date 2025-07-14 10:17:23
     */
    String uploadByWebDav(InputStream inputStream, HttpServletRequest request, String chatId, TelegramBot bot);

    /**
     * Description:
     * 通过webdav下载
     * @param path webdav路径
     * @return ResponseEntity
     * @author SkyDev
     * @date 2025-07-14 10:18:54
     */
    ResponseEntity<StreamingResponseBody> downloadByWebDav(String path);

    /**
     * Description:
     * 通过webdav删除文件
     * @param path webdav路径
     * @author SkyDev
     * @date 2025-07-14 10:19:48
     */
    void deleteByWebDav(String path);

    /**
     * Description:
     * 通过webdav路径获取文件
     * @param path webdav路径
     * @return FileInfos
     * @author SkyDev
     * @date 2025-07-14 10:20:27
     */
    List<FileInfo> listFiles(String path);
}
