package com.zmbdp.alibaba;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class AlibabaApplicationDemo {
    public static void main(String[] args) {
        SpringApplication.run(AlibabaApplicationDemo.class, args);
        log.info("AlibabaApplicationDemo 启动成功.....");
    }
}
