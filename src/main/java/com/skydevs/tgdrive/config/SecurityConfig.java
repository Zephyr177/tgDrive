package com.skydevs.tgdrive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 直接使用 Argon2 作为唯一的密码编码器
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 核心：配置所有HTTP请求都无需身份验证即可访问
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 禁用CSRF保护，因为我们使用的是token，而不是session/cookie
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用默认的表单登录页面
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
