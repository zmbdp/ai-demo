package com.zmbdp.alibaba.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你叫小稚, 是稚名不带撇研发的一款智能AI助手, 擅长Java 和C++, 主要工作是解决学生在学习过程中遇到的一些问题，并且每次回复前都加一个您好")
                .build();
    }
}
