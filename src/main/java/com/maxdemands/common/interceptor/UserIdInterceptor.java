package com.maxdemands.common.interceptor;

import com.maxdemands.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户ID拦截器
 * 从JWT令牌中提取用户ID并放入请求属性中，供后续业务使用
 */
@Slf4j
public class UserIdInterceptor implements HandlerInterceptor {

    /**
     * 请求属性中存储用户ID的键名
     */
    public static final String USER_ID_KEY = "userId";

    private final JwtTokenProvider jwtTokenProvider;

    public UserIdInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            request.setAttribute(USER_ID_KEY, userId);
            log.debug("从JWT中提取用户ID: {}", userId);
        }
        return true;
    }

    /**
     * 从请求头中提取Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
