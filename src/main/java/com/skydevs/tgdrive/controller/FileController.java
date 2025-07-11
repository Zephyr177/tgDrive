package com.skydevs.tgdrive.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.skydevs.tgdrive.dto.UploadFile;
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


@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final BotService botService;

    private final FileService fileService;


    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    public CompletableFuture<Result<UploadFile>> uploadFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (multipartFile == null || multipartFile.isEmpty()) {
                return Result.error("上传的文件为空");
            }
            return Result.success(botService.getUploadFile(multipartFile, request));
        });
    }




    /**
     * 获取文件列表
     * @param page
     * @param size
     * @return
     */
    @SaCheckLogin
    @GetMapping("/fileList")
    public Result<PageResult> getFileList(@RequestParam int page, @RequestParam int size) {
        PageResult pageResult = fileService.getFileList(page, size);
        return Result.success(pageResult);
    }

    /**
     * 更新文件url
     * @return
     */
    @SaCheckLogin
    @PutMapping("/file-url")
    public Result updateFileUrl(HttpServletRequest request) {
        log.info("更新文件url");
        fileService.updateUrl(request);
        return Result.success();
    }
}