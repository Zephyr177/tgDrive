package com.skydevs.tgdrive.config;

import com.skydevs.tgdrive.Interceptor.WebDavAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Description:
 * web配置
 * @author SkyDev
 * @date 2025-07-11 17:23:25
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final WebDavAuthInterceptor webDavAuthInterceptor;

    /**
     * Description:
     * 添加跨域配置
     * @param registry 跨域配置注册器
     * @author SkyDev
     * @date 2025-07-11 17:24:58
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    /**
     * Description:
     * webdav拦截
     * @author SkyDev
     * @date 2025-07-11 17:26:17
     */
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webDavAuthInterceptor)
                .addPathPatterns("/webdav/**");
    }
}
