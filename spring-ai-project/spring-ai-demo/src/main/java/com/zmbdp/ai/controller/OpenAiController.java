package com.zmbdp.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/openai")
public class OpenAiController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @RequestMapping("/chat")
    public String chat(String message) {
        log.info("message: {}", message);
        return openAiChatModel.call(message);
    }
//    添加代理配置信息到 添加虚拟机选项中: -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=33210 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=33210
}
