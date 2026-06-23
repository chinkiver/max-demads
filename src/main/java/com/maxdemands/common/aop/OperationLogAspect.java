package com.maxdemands.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemands.annotation.OperationLog;
import com.maxdemands.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.maxdemands.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取当前用户
        String username = "anonymous";
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
            try {
                userId = Long.valueOf(authentication.getName());
            } catch (NumberFormatException ignored) {
            }
        }

        // 获取参数
        String params = "";
        try {
            params = objectMapper.writeValueAsString(joinPoint.getArgs());
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
        } catch (JsonProcessingException e) {
            params = "参数序列化失败";
        }

        // 执行目标方法
        Object result;
        int status = 1;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            status = 0;
            throw throwable;
        } finally {
            // 保存日志
            try {
                com.maxdemands.entity.OperationLog logEntity = new com.maxdemands.entity.OperationLog();
                logEntity.setUserId(userId);
                logEntity.setUsername(username);
                logEntity.setModule(annotation.module());
                logEntity.setOperation(annotation.value());
                logEntity.setMethod(method.getDeclaringClass().getName() + "." + method.getName());
                logEntity.setParams(params);
                logEntity.setStatus(status);
                logEntity.setIp(request != null ? getIpAddress(request) : "");
                operationLogMapper.insert(logEntity);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
