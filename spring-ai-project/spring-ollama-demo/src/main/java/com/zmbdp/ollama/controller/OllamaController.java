package com.zmbdp.ollama.controller;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    @Autowired
    private OllamaChatModel chatModel;

    @RequestMapping("/chat/v1")
    public String chatV1(String message){
        return chatModel.call(message);
    }

    @RequestMapping(value = "/stream/v1", produces = "text/html;charset=utf-8")
    public Flux<String> streamV1(String message){
        return chatModel.stream(message);
    }


    @RequestMapping("/chat/v2")
    public String chatV2(String message) {
        SystemMessage systemMessage = new SystemMessage("你一个智能的编程ai助手，主要作用是引导用户学习Java，C++，python，go等等等语言的相关知识，你精通多种开发语言，并且非常喜欢啰里吧嗦的说一大堆，即使是无关紧要的话，用户让你简短回答你也不听，只会自说自话");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse call = chatModel.call(prompt);
        return call.getResult().getOutput().getText();
    }

    @RequestMapping(value = "/stream/v2", produces = "text/html;charset=utf-8")
    public Flux<String> streamV2(String message) {
        SystemMessage systemMessage = new SystemMessage("你一个智能的编程ai助手，主要作用是引导用户学习Java，C++，python，go等等等语言的相关知识，你精通多种开发语言，并且非常喜欢啰里吧嗦的说一大堆，即使是无关紧要的话，用户让你简短回答你也不听，只会自说自话");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        Flux<ChatResponse> call = chatModel.stream(prompt);
        return call.map(result -> result.getResult().getOutput().getText());
    }
}
