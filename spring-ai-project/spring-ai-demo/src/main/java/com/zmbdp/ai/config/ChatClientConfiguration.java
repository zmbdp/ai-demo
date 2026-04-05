package com.zmbdp.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {

    @Bean
    public ChatClient setChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("你叫稚名不带撇，擅长Java和C++编程相关的技术，请使用友好的语气回答问题") // 设置系统提示词
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
