package com.zmbdp.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
        // 设置 HTTP 代理
//        System.setProperty("http.proxyHost", "127.0.0.1"); // 代理地址
//        System.setProperty("http.proxyPort", "7897"); // 代理端口
//        // 设置 HTTPS 代理
//        System.setProperty("https.proxyHost", "127.0.0.1"); // 代理地址
//        System.setProperty("https.proxyPort", "7897"); // 代理端口
        log.info("SpringAiApplication 启动成功.....");
    }
}
