package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.BotService;
import com.skydevs.tgdrive.service.FileService;
import com.skydevs.tgdrive.service.WebDavService;
import com.skydevs.tgdrive.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description:
 * webdav核心业务
 * @author SkyDev
 * @date 2025-07-11 17:56:56
 */
@RestController
@Slf4j
@RequestMapping("/webdav")
@RequiredArgsConstructor
public class WebDavController {

    private final FileService fileService;

    private final BotService botService;

    private final WebDavService webDacService;

    /**
     * Description:
     * 上传文件
     * @param request 前端请求
     * @author SkyDev
     * @date 2025-07-11 17:57:20
     */
    @PutMapping("/**")
    public Result<Void> handlePut(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            fileService.uploadByWebDav(inputStream, request, botService.getChatId(), botService.getBot());
            return Result.success();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * Description:
     * 下载文件
     * @param request 前端请求
     * @author SkyDev
     * @date 2025-07-11 17:58:12
     */
    @GetMapping("/**")
    public ResponseEntity<StreamingResponseBody> handleGet(HttpServletRequest request) {
        return fileService.downloadByWebDav(request.getRequestURI().substring("/webdav".length()));
    }

    /**
     * Description:
     * 删除文件
     * @param request 前端请求
     * @param response 后端响应
     * @author SkyDev
     * @date 2025-07-11 17:58:42
     */
    @DeleteMapping("/**")
    public Result<Void> handleDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            fileService.deleteByWebDav(StringUtil.getPath(request.getRequestURI()));
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败");
        }
    }

    /**
     * Description:
     * 处理探测请求
     * @param response 后端响应
     * @author SkyDev
     * @date 2025-07-11 17:59:20
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handleOptions(HttpServletResponse response) {
        response.setHeader("Allow", "OPTIONS, HEAD, GET, POST, PROPFIND, MKCOL, MOVE, COPY");
        response.setHeader("DAV", "1,2");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Description:
     * 处理特殊的webdav方法
     * @param request 前端请求
     * @param response 后端响应
     * @author SkyDev
     * @date 2025-07-11 18:00:02
     */
    @RequestMapping(value = "/dispatch/**", method = {RequestMethod.POST})
    public void handleWebDav(HttpServletRequest request, HttpServletResponse response) throws IOException {
        webDacService.switchMethod(request, response);
    }
}
