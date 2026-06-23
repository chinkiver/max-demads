package com.maxdemands.config;

import com.maxdemands.common.interceptor.UserIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 包含CORS跨域配置和拦截器注册
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有来源（生产环境建议指定具体域名）
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带认证信息
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }

    private final com.maxdemands.util.JwtTokenProvider jwtTokenProvider;

    public WebConfig(com.maxdemands.util.JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserIdInterceptor(jwtTokenProvider))
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除登录和注册接口
                .excludePathPatterns("/auth/login", "/auth/register");
    }
}
