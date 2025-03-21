package com.skydevs.tgdrive.service.impl;

import com.alibaba.fastjson.JSON;
import com.pengrad.telegrambot.model.File;
import com.skydevs.tgdrive.entity.BigFileInfo;
import com.skydevs.tgdrive.exception.BotNotSetException;
import com.skydevs.tgdrive.mapper.FileMapper;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.DownloadService;
import com.skydevs.tgdrive.utils.OkHttpClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private BotService botService;
    @Autowired
    private FileMapper fileMapper;

    private final OkHttpClient okHttpClient = OkHttpClientFactory.createClient();

    /**
     * 下载文件
     * @param fileID
     * @return
     */
    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(String fileID) {
        try (InputStream inputStream = downloadFileInputStream(fileID);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()){
            byte[] data = new byte[8192];
            int byteRead;
            while ((byteRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, byteRead);
            }

            byte[] inputData = buffer.toByteArray();
            try (InputStream inputStream1 = new ByteArrayInputStream(inputData);
            InputStream inputStream2 = new ByteArrayInputStream(inputData)) {
                BigFileInfo record = parseBigFileInfo(inputStream1);

                if (record != null && record.isRecordFile()) {
                    return handleRecordFile(fileID, record);
                }
                return handleRegularFile(fileID, inputStream2, inputData);
            }
        } catch (IOException e) {
            log.error("下载文件失败：" + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (NullPointerException e) {
            throw new BotNotSetException();
        }
    }

    /**
     * 处理小文件
     * @param fileID
     * @param inputStream
     * @return
     */
    private ResponseEntity<StreamingResponseBody> handleRegularFile(String fileID, InputStream inputStream, byte[] chunkData) {
        log.info("文件不是记录文件，直接下载文件...");

        File file = botService.getFile(fileID);
        String filename = resolveFilename(fileID, file.filePath());
        if (filename.lastIndexOf('.') == -1) {
            Tika tika = new Tika();
            try (InputStream is = new ByteArrayInputStream(chunkData)) {
                String mimeType = tika.detect(is);

                String extension = getExtensionByMimeType(mimeType);
                if (!extension.isEmpty()) {
                    filename = filename + extension;
                } else {
                    log.error("未添加扩展名，扩展名检测失败");
                }
            } catch (Exception e) {
                log.error("文件检测失败" + e.getCause().getMessage());
            }
        }
        long fullSize = file.fileSize();

        HttpHeaders headers = setHeaders(filename, fullSize);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            streamData(inputStream, outputStream);
        };

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(getContentTypeFromFilename(filename)))
                .body(streamingResponseBody);
    }

    private String getExtensionByMimeType(String mimeType) {
        try {
            // 使用Tika的MimeType工具获取扩展名
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType type = allTypes.forName(mimeType);
            return type.getExtension();
        } catch (Exception e) {
            log.error("无法获取扩展名");
            return "";
        }
    }

    /**
     * 流数据处理
     * @param inputStream
     * @param outputStream
     */
    private void streamData(InputStream inputStream, OutputStream outputStream) {
        try (InputStream is = inputStream) {
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            handleClientAbortException(e);
        } catch (Exception e) {
            log.info("文件下载终止");
            log.info(e.getMessage(), e);
        }
    }

    /**
     * 处理大文件
     * @param fileID
     * @param record
     * @return
     */
    private ResponseEntity<StreamingResponseBody> handleRecordFile(String fileID, BigFileInfo record) {
        log.info("文件名为：" + record.getFileName());
        log.info("检测到记录文件，开始下载并合并分片文件...");

        String filename = resolveFilename(fileID, record.getFileName());
        Long fullSize = fileMapper.getFullSizeByFileId(fileID);

        HttpHeaders headers = setHeaders(filename, fullSize);

        List<String> partFileIds = record.getFileIds();

        StreamingResponseBody streamingResponseBody = outputStream -> {
            downloadAndMergeFileParts(partFileIds, outputStream);
        };

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(getContentTypeFromFilename(filename)))
                .body(streamingResponseBody);
    }

    /**
     * 下载并合并分片文件
     * @param partFileIds
     * @param outputStream
     */
    private void downloadAndMergeFileParts(List<String> partFileIds, OutputStream outputStream) {
        // 根据CPU核心数调整并发下载数，但不超过6个线程
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxConcurrentDownloads = Math.min(Math.max(availableProcessors, 8), 32);
        ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentDownloads);
        
        log.info("使用 {} 个线程并行下载分片", maxConcurrentDownloads);
        
        try {
            // 使用临时文件来存储下载的分片，避免内存问题
            Path tempDir = Files.createTempDirectory("tgdrive_download_");
            List<Path> tempFiles = new ArrayList<>(partFileIds.size());
            
            // 并行下载所有分片到临时文件
            CountDownLatch downloadLatch = new CountDownLatch(partFileIds.size());
            for (int i = 0; i < partFileIds.size(); i++) {
                final int index = i;
                final String partFileId = partFileIds.get(i);
                final Path tempFile = tempDir.resolve("part_" + index);
                tempFiles.add(tempFile);
                
                executorService.submit(() -> {
                    try {
                        log.info("开始下载第 {} 个分片文件 (ID: {})", index, partFileId);
                        downloadPartToFile(partFileId, tempFile);
                        log.info("分片 {} 下载完成", index);
                    } catch (Exception e) {
                        log.error("分片 {} 下载失败: {}", index, e.getMessage(), e);
                    } finally {
                        downloadLatch.countDown();
                    }
                });
            }
            
            // 等待所有下载完成或超时
            boolean allDownloaded = downloadLatch.await(30, TimeUnit.MINUTES);
            if (!allDownloaded) {
                log.warn("部分分片下载超时");
            }
            
            // 按顺序将临时文件写入输出流
            byte[] buffer = new byte[65536]; // 64KB
            for (int i = 0; i < tempFiles.size(); i++) {
                Path tempFile = tempFiles.get(i);
                if (!Files.exists(tempFile)) {
                    log.warn("分片 {} 的临时文件不存在，跳过", i);
                    continue;
                }
                
                try (InputStream fis = new BufferedInputStream(Files.newInputStream(tempFile), 131072)) { // 128KB buffer
                    int bytesRead;
                    int totalBytesRead = 0;
                    // 每50MB才flush一次
                    int flushThreshold = 52428800;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        try {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            // 只在达到阈值时才flush，提高传输效率
                            if (totalBytesRead >= flushThreshold) {
                                outputStream.flush();
                                totalBytesRead = 0;
                            }
                        } catch (IOException e) {
                            if (isClientDisconnected(e)) {
                                log.info("客户端已断开连接，停止合并分片");
                                return;
                            }
                            throw e;
                        }
                    }
                } catch (IOException e) {
                    if (isClientDisconnected(e)) {
                        log.info("客户端已断开连接，停止合并分片");
                        return;
                    }
                    throw new RuntimeException("合并分片文件失败: " + e.getMessage(), e);
                } finally {
                    // 处理完后删除临时文件
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        log.warn("无法删除临时文件: {}", tempFile, e);
                    }
                }
            }
            
            // 确保所有数据都写入
            try {
                outputStream.flush();
            } catch (IOException e) {
                if (!isClientDisconnected(e)) {
                    throw new RuntimeException("最终flush失败: " + e.getMessage(), e);
                }
            }
            
            log.info("所有分片下载并合并完成");
            
            // 清理临时目录
            try {
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                log.warn("无法删除临时目录: {}", tempDir, e);
            }
        } catch (Exception e) {
            log.error("文件下载过程中发生错误", e);
            throw new RuntimeException("文件下载终止: " + e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
    }
    
    /**
     * 下载分片文件到临时文件
     * @param partFileId 分片文件ID
     * @param tempFile 临时文件路径
     * @throws IOException 下载失败时抛出
     */
    private void downloadPartToFile(String partFileId, Path tempFile) throws IOException {
        // 使用专用的下载客户端
        try (ResponseBody responseBody = downloadFileByte(partFileId);
             BufferedInputStream bis = new BufferedInputStream(responseBody.byteStream(), 131072); // 128KB buffer
             BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(tempFile), 131072)) {
              
            byte[] buffer = new byte[65536]; // 64KB
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }
    }
    
    /**
     * 检查是否为客户端断开连接的异常
     * @param e IOException实例
     * @return 如果是客户端断开连接返回true
     */
    private boolean isClientDisconnected(IOException e) {
        if (e == null) return false;
        
        String message = e.getMessage();
        if (message == null) return false;
        
        // 检查各种可能的客户端断开连接的错误信息
        return message.contains("Broken pipe") 
               || message.contains("Connection reset by peer")
               || message.contains("你的主机中的软件中止了一个已建立的连接") 
               || message.contains("Connection reset")
               || message.contains("connection was aborted")
               || message.contains("Pipe closed")
               || (e.getCause() != null && e.getCause().getMessage() != null && 
                   (e.getCause().getMessage().contains("Broken pipe") || 
                    e.getCause().getMessage().contains("Connection reset")));
    }

    /**
     * 处理客户端终止连接异常
     * @param e
     */
    private void handleClientAbortException(IOException e) {
        if (isClientDisconnected(e)) {
            log.info("客户端中止了连接");
            // 不抛出异常，让程序正常结束
        } else {
            log.error("写入输出流时发生 IOException", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理文件名
     * @param fileID
     * @param defaultName
     * @return
     */
    private String resolveFilename(String fileID, String defaultName) {
        String filename = fileMapper.getFileNameByFileId(fileID);
        if (filename == null) {
            filename = defaultName;
        }

        return filename;
    }

    /**
     * 尝试转换为大文件的记录文件
     * @param inputStream 下载的文件的输入流
     * @return BigFilInfo
     */
    private BigFileInfo parseBigFileInfo(InputStream inputStream) {
        try {
            String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return JSON.parseObject(fileContent, BigFileInfo.class);
        } catch (Exception e) {
            log.info("文件不是 BigFileInfo类型，作为普通文件处理");
            return null;
        }
    }

    /**
     * 下载文件并转换为流处理
     * @param fileID
     * @return
     * @throws IOException
     */
    private InputStream downloadFileInputStream(String fileID) throws IOException {
        File file = botService.getFile(fileID);
        String fileUrl = botService.getFullDownloadPath(file);

        Request request = new Request.Builder()
                .url(fileUrl)
                .get()
                .build();

        // 使用专用的下载客户端
        OkHttpClient downloadClient = OkHttpClientFactory.createDownloadClient();
        Response response = downloadClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            log.error("无法下载文件，响应码：" + response.code());
            throw new IOException("无法下载文件，响应码：" + response.code());
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            log.error("响应体为空");
            throw new IOException("响应体为空");
        }

        // 使用BufferedInputStream包装原始流以提高性能
        return new BufferedInputStream(responseBody.byteStream(), 65536); // 64KB buffer
    }

    /**
     * 设置响应头
     *
     * @param filename
     * @param size
     * @return
     */
    private HttpHeaders setHeaders(String filename, Long size) {
        HttpHeaders headers = new HttpHeaders();
        try {
            String contentType = getContentTypeFromFilename(filename);
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (size != null && size > 0) {
                headers.setContentLength(size);
            }

            if (contentType.startsWith("image/") || contentType.startsWith("video/")) {
                // 对于图片和视频，设置 Content-Disposition 为 inline
                headers.setContentDisposition(ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build());
            } else {
                // 使用 URLEncoder 编码文件名，确保支持中文
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                String contentDisposition = "attachment; filename*=UTF-8''" + encodedFilename;
                headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("不支持的编码");
        }
        return headers;
    }

    /**
     * 下载分片文件
     *
     * @param partFileId
     * @return
     * @throws IOException
     */
    private ResponseBody downloadFileByte(String partFileId) throws IOException {
        // 使用专用的下载客户端
        OkHttpClient downloadClient = OkHttpClientFactory.createDownloadClient();
        
        File partFile = botService.getFile(partFileId);
        String partFileUrl = botService.getFullDownloadPath(partFile);
        Request partRequest = new Request.Builder()
                .url(partFileUrl)
                .get()
                .build();

        try {
            Response response = downloadClient.newCall(partRequest).execute();
            if (!response.isSuccessful()) {
                log.error("无法下载分片文件，响应码：" + response.code());
                throw new IOException("无法下载分片文件，响应码：" + response.code());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("分片响应体为空");
                throw new IOException("分片响应体为空");
            }

            return responseBody;
        } catch (Exception e) {
            log.error("下载分片文件时发生错误: {}", e.getMessage());
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("下载分片文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件类型
     *
     * @param filename
     * @return
     */
    private String getContentTypeFromFilename(String filename) {
        String contentType = null;
        Path path = Paths.get(filename);
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            log.warn("无法通过 Files.probeContentType 获取 MIME 类型: " + e.getMessage());
        }

        if (contentType == null) {
            // 手动映射常见的文件扩展名到 MIME 类型
            String extension = getFileExtension(filename).toLowerCase();
            contentType = switch (extension) {
                case "gif" -> "image/gif";
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "bmp" -> "image/bmp";
                case "txt" -> "text/plain";
                case "pdf" -> "application/pdf";
                case "mp4" -> "video/mp4";
                // 添加其他需要的类型
                default -> "application/octet-stream";
            };
        }
        return contentType;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}