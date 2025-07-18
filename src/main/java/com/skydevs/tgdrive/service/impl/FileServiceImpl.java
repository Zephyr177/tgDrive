package com.skydevs.tgdrive.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.SendResponse;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.exception.BotNotSetException;
import com.skydevs.tgdrive.exception.FailedToGetSizeException;
import com.skydevs.tgdrive.exception.NoConnectionException;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.result.PageResult;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.DownloadService;
import com.skydevs.tgdrive.service.FileService;
import com.skydevs.tgdrive.utils.StringUtil;
import com.skydevs.tgdrive.utils.UserFriendly;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Description:
 * 文件相关逻辑
 * @author SkyDev
 * @date 2025-07-14 10:14:11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileMapper fileMapper;
    private final BotService botService;
    private final DownloadService downloadService;

    // 控制同时运行的任务数量
    private final int PERMITS = 5;
    // tg bot接口限制20MB，传10MB是最佳实践
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Description:
     * 获取文件分页
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @return FileInfos
     * @author SkyDev
     * @date 2025-07-14 10:14:57
     */
    @Override
    public PageResult<FileInfo> getFileList(int pageNum, int pageSize) {
        // 设置分页
        try (Page<Object> page = PageHelper.startPage(pageNum, pageSize)) {
            Page<FileInfo> pageInfo = fileMapper.getAllFiles();
            List<FileInfo> fileInfos = new ArrayList<>();
            for (FileInfo fileInfo : pageInfo) {
                FileInfo fileInfo1 = new FileInfo();
                BeanUtils.copyProperties(fileInfo, fileInfo1);
                fileInfos.add(fileInfo1);
            }
            log.info("文件分页查询");
            return new PageResult<>((int) pageInfo.getTotal(), fileInfos);
        }
    }

    /**
     * Description:
     * 更新文件url
     * @param request 请求
     * @author SkyDev
     * @date 2025-07-14 10:16:51
     */
    @Override
    public void updateUrl(HttpServletRequest request) {
        String prefix = StringUtil.getPrefix(request);
        fileMapper.updateUrl(prefix);
    }


    /**
     * 获取完整下载路径
     *
     * @param file
     * @return
     */
    public String getFullDownloadPath(File file, TelegramBot bot) {
        log.info("获取完整的下载路径: " + bot.getFullFilePath(file));
        return bot.getFullFilePath(file);
    }

    /**
     * 生成上传文件
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @Override
    public UploadFile getUploadFile(MultipartFile multipartFile, HttpServletRequest request, String chatId, TelegramBot bot) {
        UploadFile uploadFile = new UploadFile();
        if (!multipartFile.isEmpty()) {
            String downloadUrl = uploadFileByWebDav(multipartFile, request, chatId, bot);
            uploadFile.setFileName(multipartFile.getOriginalFilename());
            uploadFile.setDownloadLink(downloadUrl);
        } else {
            uploadFile.setFileName("文件不存在");
        }

        return uploadFile;
    }

    @Override
    public void deleteFiles(List<String > fileIds) {
        fileMapper.deleteFilesByIds(fileIds);
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @param request
     * @return 文件下载地址
     */
    private String uploadFileByWebDav(MultipartFile multipartFile, HttpServletRequest request, String chatId, TelegramBot bot) {
        try {
            String prefix = StringUtil.getPrefix(request);
            InputStream inputStream = multipartFile.getInputStream();
            String filename = multipartFile.getOriginalFilename();
            long size = multipartFile.getSize();
            if (size > MAX_FILE_SIZE) {
                List<String> fileIds = sendFileStreamInChunks(inputStream, filename, chatId, bot);
                String fileID = createRecordFile(filename, size, fileIds, chatId, bot);
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(fileID)
                        .size(UserFriendly.humanReadableFileSize(size))
                        .fullSize(size)
                        .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        .downloadUrl(prefix + "/d/" + fileID)
                        .fileName(filename)
                        .build();
                fileMapper.insertFile(fileInfo);
                return prefix + "/d/" + fileID;
            } else {
                // 小于10MB的GIF会被TG转换为MP4，对文件后缀进行处理
                String uploadFilename = filename;
                if (filename != null && filename.endsWith(".gif")) {
                    uploadFilename = filename.substring(0, filename.lastIndexOf(".gif"));
                }
                String fileID = uploadOneFile(inputStream, uploadFilename, chatId, bot);
                FileInfo fileInfo = FileInfo.builder()
                        .fileId(fileID)
                        .size(UserFriendly.humanReadableFileSize(size))
                        .fullSize(size)
                        .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        .downloadUrl(prefix + "/d/" + fileID)
                        .fileName(filename)
                        .build();
                fileMapper.insertFile(fileInfo);
                return prefix + "/d/" + fileID;
            }
        } catch (IOException e) {
            log.error("文件上传失败，响应信息：{}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 上传单文件（为了使gif能正常显示，gif上传到tg后，会被转换为MP4）
     * @param inputStream
     * @param filename
     * @return
     */
    private String uploadOneFile(InputStream inputStream, String filename, String chatId, TelegramBot bot) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int byteRead;
            while ((byteRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, byteRead);
            }
            byte[] chunkData = buffer.toByteArray();
            return uploadChunk(chunkData, filename, chatId, bot);
        } catch (IOException e) {
            log.error("文件上传失败 :" + e.getMessage());
            return null;
        }
    }

    /**
     * 生成recordFile
     *
     * @param originalFileName
     * @param fileSize
     * @param fileIds
     * @return
     * @throws IOException
     */
    private String createRecordFile(String originalFileName, long fileSize, List<String> fileIds, String chatId, TelegramBot bot) throws IOException {
        BigFileInfo record = new BigFileInfo();
        record.setFileName(originalFileName);
        record.setFileSize(fileSize);
        record.setFileIds(fileIds);
        record.setRecordFile(true);

        // 创建一个系统临时文件，不依赖特定路径
        Path tempDir = Files.createTempDirectory("tempDir");
        String hashString = DigestUtil.sha256Hex(originalFileName);
        Path tempFile = tempDir.resolve(hashString + ".record.json");
        Files.createFile(tempFile);
        try {
            String jsonString = JSON.toJSONString(record, true);
            Files.write(Paths.get(tempFile.toUri()), jsonString.getBytes());
        } catch (IOException e) {
            log.error("上传记录文件生成失败" + e.getMessage());
            throw new RuntimeException("上传文件生成失败");
        }

        // 上传记录文件到 Telegram
        byte[] fileBytes = Files.readAllBytes(tempFile);
        SendDocument sendDocument = new SendDocument(chatId, fileBytes)
                .fileName(tempFile.getFileName().toString());

        SendResponse response = bot.execute(sendDocument);
        Message message = response.message();
        String recordFileId = message.document().fileId();

        log.info("记录文件上传成功，File ID: " + recordFileId);

        // 删除本地临时文件
        Files.deleteIfExists(tempFile);

        return recordFileId;
    }

    /**
     * 分块上传文件
     *
     * @param inputStream
     * @param filename
     * @return
     */
    private List<String> sendFileStreamInChunks(InputStream inputStream, String filename, String chatId, TelegramBot bot) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(PERMITS); // 线程池大小
        Semaphore semaphore = new Semaphore(PERMITS); // 控制同时运行的任务数量

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[MAX_FILE_SIZE]; // 10MB 缓冲区
            int partIndex = 0;

            while (true) {
                // 用offset追踪buffer读了多少字节
                int offset = 0;
                while(offset < MAX_FILE_SIZE) {
                    int byteRead = bufferedInputStream.read(buffer, offset, MAX_FILE_SIZE - offset);
                    if (byteRead == -1) {
                        break;
                    }
                    offset += byteRead;
                }

                if (offset == 0) {
                    break;
                }
                semaphore.acquire(); // 获取许可，若没有可用许可则阻塞

                // 当前块的文件名
                String partName = filename + "_part" + partIndex;
                partIndex++;

                // 取当前分块数据
                byte[] chunkData = Arrays.copyOf(buffer, offset);

                // 提交上传任务，使用CompletableFuture
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String fileId = uploadChunk(chunkData, partName, chatId, bot);
                        if (fileId == null) {
                            throw new RuntimeException("分块 " + partName + " 上传失败");
                        }
                        return fileId;
                    } finally {
                        semaphore.release(); // 在任务完成后释放信号量
                    }
                }, executorService);
                futures.add(future);
            }

            // 等待所有任务完成并按顺序获取结果
            List<String> fileIds = new ArrayList<>();
            try {
                for (CompletableFuture<String> future : futures) {
                    fileIds.add(future.join()); // 按顺序等待结果
                }
                return fileIds;
            } catch (CompletionException e) {
                for (CompletableFuture<String> future : futures) {
                    future.cancel(true);
                }
                executorService.shutdown();
                throw new RuntimeException("分块上传失败: " + e.getCause().getMessage(), e);
            }
        } catch (IOException | InterruptedException e) {
            log.error("文件流读取失败或上传失败：{}", e.getMessage());
            throw new RuntimeException("文件流读取失败或上传");
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * 上传块
     *
     * @param chunkData
     * @param partName
     * @return
     * @throws EOFException
     */
    private String uploadChunk(byte[] chunkData, String partName, String chatId, TelegramBot bot) {
        SendDocument sendDocument = new SendDocument(chatId, chunkData).fileName(partName);
        int retryCount = 3;
        int baseDelay = 1000; // 基础延迟时间（毫秒）

        for (int i = 0; i < retryCount; i++) {
            try {
                SendResponse response = bot.execute(sendDocument);

                // 检查响应
                if (response != null && response.isOk() && response.message() != null) {
                    // 安全地获取fileId
                    String fileID;
                    fileID = extractFileId(response.message());
                    if (fileID != null) {
                        log.info("分块上传成功，File ID：{}， 文件名：{}", fileID, partName);
                        return fileID;
                    }
                }

                // 如果到这里，说明上传没有成功，需要重试
                int exponentialDelay = baseDelay * (int)Math.pow(2, i); // 指数退避策略
                log.warn("上传失败，正在准备第{}次重试，等待{}毫秒", (i+1), exponentialDelay);
                Thread.sleep(exponentialDelay);

            } catch (NullPointerException e) {
                log.error("Bot未设置或其他空指针异常", e);
                throw new BotNotSetException("Bot未设置或其他空指针异常");
            } catch (InterruptedException e) {
                log.error("线程被中断", e);
                Thread.currentThread().interrupt(); // 重置中断状态
                throw new RuntimeException("上传过程被中断", e);
            } catch (Exception e) {
                // 捕获所有其他异常
                log.error("上传过程中发生未预期的异常: {}", e.getMessage(), e);

                try {
                    int exponentialDelay = baseDelay * (int)Math.pow(2, i); // 指数退避策略
                    log.warn("发生异常，正在准备第{}次重试，等待{}毫秒", (i+1), exponentialDelay);
                    Thread.sleep(exponentialDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断", ie);
                }

                // 如果是最后一次重试，则抛出异常
                if (i == retryCount - 1) {
                    throw new RuntimeException("上传失败，已达到最大重试次数", e);
                }
            }
        }

        // 如果所有重试都失败
        log.error("分块上传失败，已重试{}次，文件名：{}", retryCount, partName);
        throw new NoConnectionException("无法上传文件分块，已达到最大重试次数");
    }

    /**
     * Description:
     * 提取正确的fileID
     * @author SkyDev
     * @date 2025-07-17 11:44:50
     * @return fileID
     * @param message telegram的响应消息
     */
    public String extractFileId(Message message) {
        if (message == null) {
            return null;
        }
        log.info("message_id:" + " " + message.messageId());

        // 按优先级检查可能的文件类型
        if (message.document() != null) {
            return message.document().fileId();
        } else if (message.sticker() != null) {
            return message.sticker().fileId();
        } else if (message.video() != null) {
            return message.video().fileId();
        } else if (message.photo() != null && message.photo().length > 0) {
            return message.photo()[message.photo().length - 1].fileId(); // 取最后一张（通常是最高分辨率）
        } else if (message.audio() != null) {
            return message.audio().fileId();
        } else if (message.animation() != null) {
            return message.animation().fileId();
        } else if (message.voice() != null) {
            return message.voice().fileId();
        } else if (message.videoNote() != null) {
            return message.videoNote().fileId();
        }

        return null; // 没有找到 fileId
    }

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
    private String uploadFileByWebDav(InputStream inputStream, String path, HttpServletRequest request, String chatId, TelegramBot bot) {
        try {
            String filename = path.substring(path.lastIndexOf('/') + 1);
            long size = request.getContentLengthLong();

            return getUploadedFileID(inputStream, filename, size, chatId, bot);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Nullable
    private String getUploadedFileID(InputStream inputStream, String filename, long size, String chatId, TelegramBot bot) throws IOException {
        if (size > MAX_FILE_SIZE) {
            List<String> fileIds = sendFileStreamInChunks(inputStream, filename, chatId, bot);
            return createRecordFile(filename, size, fileIds, chatId, bot);
        } else {
            String uploadFilename = filename;
            if (filename.endsWith(".gif")) {
                uploadFilename = filename.substring(0, filename.lastIndexOf(".gif"));
            }
            return uploadOneFile(inputStream, uploadFilename, chatId, bot);
        }
    }

    /**
     * Description:
     * 通过webdav上传文件
     * @param inputStream 文件输入流
     * @param request 上传文件请求
     * @return fileId
     * @author SkyDev
     * @date 2025-07-14 10:17:23
     */
    @Override
    public String uploadByWebDav(InputStream inputStream, HttpServletRequest request, String chatId, TelegramBot bot) {
        try {
            String path = StringUtil.getPath(request.getRequestURI());

            long size = request.getContentLengthLong();
            if (size < 0) {
                log.error("无法获取文件大小");
                throw new FailedToGetSizeException();
            }
            String fileId = uploadFileByWebDav(inputStream, path, request, chatId, bot);
            List<FileInfo> fileInfos = fileMapper.getFilesByPathPrefix(path);
            for (FileInfo fileInfo : fileInfos) {
                fileMapper.deleteFile(fileInfo.getFileId());
            }
            // 提取文件夹名字（如果有文件夹的话）
            List<String> dirPaths = StringUtil.getDirsPathFromPath(path);
            for (String dirPath : dirPaths) {
                FileInfo dirInfo = fileMapper.getFileByWebdavPath(dirPath);
                if (dirInfo != null) {
                    continue;
                }
                dirInfo = FileInfo.builder().fileId("dir")
                        .fileName(StringUtil.getDisplayName(dirPath, true))
                        .downloadUrl("dir")
                        .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        .size("0")
                        .fullSize(0L)
                        .webdavPath(dirPath)
                        .dir(true)
                        .build();
                fileMapper.insertFile(dirInfo);
                log.info("新增文件夹路径{}", dirPath);
            }

            // 从路径中提取文件名
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            FileInfo fileInfo = FileInfo.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .fullSize(size)
                    .size(UserFriendly.humanReadableFileSize(size))
                    .uploadTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                    .downloadUrl(StringUtil.getPrefix(request) + "/d/" + fileId)
                    .webdavPath(path).build();
            fileMapper.insertFile(fileInfo);
            return fileId;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * Description:
     * 通过webdav下载
     * @param path webdav路径
     * @return ResponseEntity
     * @author SkyDev
     * @date 2025-07-14 10:18:54
     */
    @Override
    public ResponseEntity<StreamingResponseBody> downloadByWebDav(String path) {
        try {
            FileInfo fileInfo = fileMapper.getFileByWebdavPath(path);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return downloadService.downloadFile(fileInfo.getFileId());
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Description:
     * 通过webdav删除文件
     * @param path webdav路径
     * @author SkyDev
     * @date 2025-07-14 10:19:48
     */
    @Override
    public void deleteByWebDav(String path) {
        try {
            fileMapper.deleteFileByWebDav(path);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * Description:
     * 通过webdav路径获取文件
     * @param path webdav路径
     * @return FileInfos
     * @author SkyDev
     * @date 2025-07-14 10:20:27
     */
    @Override
    public List<FileInfo> listFiles(String path) {
        List<FileInfo> files = fileMapper.getFilesByPathPrefix(path);
        if (files == null) {
            log.error("文件查询失败");
            return null;
        }
        List<FileInfo> res = new ArrayList<>();
        for (FileInfo file : files) {
            String str = file.getWebdavPath().substring(path.length());
            if (str.indexOf('/') != -1 && !file.isDir()) {
                continue;
            }
            if (str.indexOf('/') != -1 && str.substring(str.indexOf('/')).length() > 1) {
                continue;
            }
            if (file.getWebdavPath().equals(path)) {
                continue;
            }
            res.add(file);
        }
        return res;
    }
}
