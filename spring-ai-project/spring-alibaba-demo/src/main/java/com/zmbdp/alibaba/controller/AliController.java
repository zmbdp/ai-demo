package com.zmbdp.alibaba.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ali")
public class AliController {

    private final ChatModel chatModel;

    public AliController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @RequestMapping("/chat")
    public String chat(String message) {
        return chatModel.call(message);
    }
}
