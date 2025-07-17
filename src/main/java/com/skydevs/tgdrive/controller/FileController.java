package com.skydevs.tgdrive.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.skydevs.tgdrive.dto.UploadFile;
import com.skydevs.tgdrive.entity.FileInfo;
import com.skydevs.tgdrive.result.PageResult;
import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;


/**
 * Description:
 * 文件管理
 * @author SkyDev
 * @date 2025-07-11 17:47:55
 */
@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final BotService botService;

    private final FileService fileService;


    /**
     * Description:
     * 上传文件
     * @author SkyDev
     * @date 2025-07-11 17:48:15
     */
    @PostMapping("/upload")
    public CompletableFuture<Result<UploadFile>> uploadFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (multipartFile == null || multipartFile.isEmpty()) {
                return Result.error("上传的文件为空");
            }
            return Result.success(fileService.getUploadFile(multipartFile, request, botService.getChatId(), botService.getBot()));
        });
    }

    /**
     * Description:
     * 获取文件分页列表
     * @param page 页数
     * @param size 每页数量
     * @author SkyDev
     * @date 2025-07-11 17:48:41
     */
    @SaCheckLogin
    @GetMapping("/fileList")
    public Result<PageResult<FileInfo>> getFileList(@RequestParam int page, @RequestParam int size) {
        PageResult<FileInfo> pageResult = fileService.getFileList(page, size);
        return Result.success(pageResult);
    }

    /**
     * Description:
     * 更新文件URL
     * @param request 前端请求
     * @return Result
     * @author SkyDev
     * @date 2025-07-11 17:50:08
     */
    @SaCheckLogin
    @PutMapping("/file-url")
    public Result<Void> updateFileUrl(HttpServletRequest request) {
        log.info("更新文件url");
        fileService.updateUrl(request);
        return Result.success();
    }
}