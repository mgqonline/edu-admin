package com.eduadmin.wechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.eduadmin")
@MapperScan("com.eduadmin.wechat.mapper")
public class WechatApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WechatApiApplication.class, args);
    }
}