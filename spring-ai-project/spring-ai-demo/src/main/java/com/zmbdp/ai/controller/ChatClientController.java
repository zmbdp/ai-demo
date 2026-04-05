package com.zmbdp.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ChatClientController {


    @Autowired
    private ChatClient chatClient;

    @RequestMapping("/chat")
    public String chat(String userInput) {
        return this.chatClient
                .prompt() // 构建一次对话请求
                .user(userInput) // 设置用户提示词（用户输入）
                .call() // 调用大模型
                .content(); // 返回响应
    }

    record Recipe(String dish, List<String> ingredients) {}

    @RequestMapping("/entity")
    public String entity(String userInput) {
        Recipe recipe = chatClient
                .prompt() // 构建一次对话请求
                .user(String.format("请帮我生成%s的食谱", userInput)) // 提示词
                .call() // 调用大模型
                .entity(Recipe.class);
        return recipe.toString();
    }

    @RequestMapping(value = "/stream",produces = "text/html;charset=utf-8")
    public Flux<String> stream(String userInput) {
        return this.chatClient
                .prompt() // 构建一次对话请求
                .user(userInput) // 设置用户提示词（用户输入）
                .stream() // 调用大模型
                .content(); // 返回响应
    }
}
