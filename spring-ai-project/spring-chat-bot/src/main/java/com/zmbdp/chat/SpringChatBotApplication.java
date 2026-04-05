package com.zmbdp.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringChatBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringChatBotApplication.class, args);
        log.info("SpringChatBotApplication 启动成功.....");
    }
}
