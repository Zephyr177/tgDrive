package com.skydevs.tgdrive.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface WebDavService {
    /**
     * Description:
     * 选择合适的方法
     * @author SkyDev
     * @param request WebDAV请求
     * @param response WebDAV响应
     */
   void switchMethod(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
