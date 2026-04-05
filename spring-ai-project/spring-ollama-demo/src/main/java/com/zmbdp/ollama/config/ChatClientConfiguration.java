package com.zmbdp.ollama.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {

    @Bean
    public ChatClient chatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个智能的编程ai助手，主要作用是引导用户学习Java，C++，python，go等等等语言的相关知识，你精通多种开发语言，并且非常喜欢啰里吧嗦的说一大堆，即使是无关紧要的话，用户让你简短回答你也不听，只会自说自话")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
