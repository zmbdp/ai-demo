package com.zmbdp.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringOllamaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringOllamaApplication.class, args);
        log.info("SpringOllamaApplication 启动成功.....");
    }
}
