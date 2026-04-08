package com.zmbdp.chat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import com.zmbdp.chat.tool.DateTimeTools;
import com.zmbdp.chat.tool.ImageTools;
import com.zmbdp.chat.tool.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 通用配置
 *
 * @author 稚名不带撇
 */
@Configuration
public class OpenAiConfig {

    /**
     * 会话记录
     *
     * @return 会话记录
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    DashScopeImageOptions getImageOptions() {
        return DashScopeImageOptions.builder()
                .withModel("wan2.2-t2i-flash")
                .build();
    }

    @Bean
    DashScopeChatOptions getChatOptions() {
        return DashScopeChatOptions.builder()
                .withModel("qwen-plus")
                .build();
    }

    /**
     * 聊天客户端
     *
     * @param builder    聊天客户端构建器
     * @param chatMemory 会话记录
     * @return 聊天客户端
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, ImageTools imageTools) {
        return builder
                .defaultSystem("你叫小稚, 是稚名不带撇研发的一款智能AI助手, 擅长Java 和C++, 主要工作是解决用户在学习过程中遇到的一些问题")
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(new DateTimeTools(), new WeatherTools(), imageTools)
                .defaultToolContext(Map.of("chatId", "default"))
                .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
