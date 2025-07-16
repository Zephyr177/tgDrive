package com.skydevs.tgdrive.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface DownloadService {
    /**
     * Description:
     * 下载文件
     * @author SkyDev
     * @param fileID 文件id
     */
    ResponseEntity<StreamingResponseBody> downloadFile(String fileID);
}
