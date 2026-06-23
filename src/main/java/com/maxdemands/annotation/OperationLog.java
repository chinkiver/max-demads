package com.maxdemands.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在需要记录日志的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作模块
     */
    String module() default "";
}
