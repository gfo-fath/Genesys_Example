package com.gfo.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 配置API安全规则，允许演示环境的公开访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // 禁用Spring Security CORS（使用WebConfig）
            .csrf(csrf -> csrf.disable()) // 禁用CSRF保护（演示环境）
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").permitAll() // 允许所有API访问
                .anyRequest().permitAll() // 允许其他所有请求
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
            );

        return http.build();
    }
}