package com.skydevs.tgdrive.Filter;

import com.skydevs.tgdrive.utils.WebDavHttpServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Description:
 * 拦截WebDav方法，将其转化为Spring可识别的处理逻辑
 * @author SkyDev
 * @date 2025-07-14 09:40:37
 */
@Component
@Slf4j
public class WebDavFilter implements Filter {
    /**
     * Description:
     * 拦截WebDav方法，将其转化为Spring可识别的处理逻辑
     * @param servletRequest 封装了客户端HTTP请求的对象，可以从中获取请求头、参数等信息。
     * @param servletResponse 封装了服务器对客户端HTTP响应的对象，可以用来向客户端输出内容。
     * @param filterChain 用于调用过滤器链中的下一个过滤器，如果这是最后一个过滤器，则调用目标资源。
     * @author SkyDev
     * @date 2025-07-14 09:41:32
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request =  (HttpServletRequest) servletRequest;
        String method = request.getMethod();

        // 拦截 PROPFIND MKCOL MOVE等WebDav方法
        if ("PROPFIND".equalsIgnoreCase(method)
        || "MKCOL".equalsIgnoreCase(method)
        || "MOVE".equalsIgnoreCase(method)
        || "COPY".equalsIgnoreCase(method)) {
            log.info("拦截到WebDAV请求: {}", method);

            // 把原始方法放到attribute
            request.setAttribute("X-HTTP-Method-Override", method);

            // 包装request，强制返回POST
            HttpServletRequest wrapper = new WebDavHttpServletRequestWrapper(request);

            String uri = request.getRequestURI();
            String forwardPath = "/webdav/dispatch" + uri.substring("/webdav".length());
            log.info("Forward To: {}", forwardPath);

            request.getRequestDispatcher(forwardPath).forward(wrapper, servletResponse);
        } else {
            filterChain.doFilter(request, servletResponse);
        }
    }
}
