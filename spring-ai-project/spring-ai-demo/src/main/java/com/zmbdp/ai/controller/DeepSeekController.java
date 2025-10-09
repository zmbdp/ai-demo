package com.zmbdp.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/deepseek")
public class DeepSeekController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @RequestMapping("/chat")
    public String chat(String message) {
        log.info("message: {}", message);
        return openAiChatModel.call(message);
    }
}
