package com.maxdemands;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 需求管理平台启动类
 */
@SpringBootApplication
@MapperScan("com.maxdemands.mapper")
public class MaxDemandsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaxDemandsApplication.class, args);
    }
}
