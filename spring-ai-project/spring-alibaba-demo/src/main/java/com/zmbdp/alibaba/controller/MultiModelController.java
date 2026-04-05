package com.zmbdp.alibaba.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

@RequestMapping("/multi")
@RestController
public class MultiModelController {

    private final ChatClient chatClient;

    public MultiModelController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @RequestMapping(value = "/image", produces = "text/html;charset=utf-8")
    public Flux<String> image(String prompt) throws Exception {
        String url = "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, new URI(url).toURL().toURI()));
        // 定义用户提示词
        UserMessage userMessage = UserMessage.builder().text(prompt).media(mediaList).build();
        // 调用 AI 模型
        return this.chatClient.prompt(new Prompt(userMessage)).stream().content();
    }
}
