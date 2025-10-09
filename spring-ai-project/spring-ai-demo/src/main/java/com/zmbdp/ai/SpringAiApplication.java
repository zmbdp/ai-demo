package com.zmbdp.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
        log.info("SpringAiApplication 启动成功.....");
    }
}
