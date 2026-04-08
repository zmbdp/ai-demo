package com.zmbdp.chat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.zmbdp.chat.domain.dto.ChatMessageDTO;
import com.zmbdp.chat.domain.vo.ChatInfo;
import com.zmbdp.chat.domain.vo.MessageVO;
import com.zmbdp.chat.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * 聊天客户端
     */
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private DashScopeChatOptions chatOptions;

    /**
     * 会话记录
     */
    @Autowired
    private ChatMemory chatMemory;

    /**
     * 聊天记录
     */
    @Autowired
    private IChatService chatService;

    /**
     * 按 URL 后缀粗略推断图片 MIME，供 Spring AI {@link Media} 使用。
     */
    private static MimeType mimeTypeForImageUrl(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        if (lower.endsWith(".gif")) {
            return MimeTypeUtils.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MimeTypeUtils.parseMimeType("image/webp");
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }

    /**
     * 构造 Spring AI 的 conversationId。
     * 为避免不同用户共享同一个 chatId 导致内存上下文串话，这里必须引入 userId 做隔离。
     */
    private static String buildConversationId(Long userId, String chatId) {
        return userId + ":" + chatId;
    }

    /**
     * 聊天接口
     *
     * @param prompt 输入内容
     * @param chatId 聊天 id
     * @param userId 用户 id
     * @return 聊天结果
     */
    @RequestMapping(value = "/stream", produces = "text/html;charset=utf-8")
    public Flux<String> stream(String prompt, String chatId, @RequestParam(defaultValue = "1") Long userId) throws Exception {
        String imageUrl = prompt.contains("oss") ? "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg" : null;
        log.info("userId: {}, chatId: {}, prompt: {}, imageUrl: {}", userId, chatId, prompt, imageUrl);
        String conversationId = buildConversationId(userId, chatId);
        syncHistoryToMemory(userId, chatId, conversationId);
        chatService.save(userId, chatId, prompt);
        List<String> userMediaUrls = (imageUrl == null || imageUrl.isBlank()) ? null : List.of(imageUrl.trim());
        chatService.saveMessage(userId, chatId, "user", prompt, userMediaUrls);

        Flux<String> contentFlux;
        if (imageUrl == null || imageUrl.isBlank()) {
            contentFlux = this.chatClient.prompt(new Prompt(prompt, chatOptions))
//                    .user(prompt)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content();
        } else {
            log.info("使用图片进行聊天, userId: {}, chatId: {}, prompt: {}, imageUrl: {}", userId, chatId, prompt, imageUrl);
            List<Media> mediaList = List.of(new Media(mimeTypeForImageUrl(imageUrl), new URI(imageUrl.trim()).toURL().toURI()));
            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .media(mediaList)
                    .build();
            contentFlux = this.chatClient.prompt(new Prompt(userMessage, chatOptions))
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content();
        }

        StringBuilder assistantReply = new StringBuilder();
        return contentFlux
                .doOnNext(assistantReply::append)
                .doOnComplete(() -> {
                    if (!assistantReply.isEmpty()) {
                        chatService.saveMessage(userId, chatId, "assistant", assistantReply.toString(), null);
                    }
                });
    }

    /**
     * 获取会话列表
     *
     * @param userId 用户 id
     * @return 会话列表
     */
    @RequestMapping("/getChatIds")
    public List<ChatInfo> sessionHistory(@RequestParam(defaultValue = "1") Long userId) {
        return chatService.getSessionHistory(userId)
                .stream()
                .map(dto -> new ChatInfo(dto.getId(), dto.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 根据聊天 id 获取会话记录
     *
     * @param chatId 聊天 id
     * @param userId 用户 id
     */
    @RequestMapping("/getChatHistory")
    public List<MessageVO> getMessageHistory(String chatId, @RequestParam(defaultValue = "1") Long userId) {
        log.info("获取会话记录, userId: {}, chatId: {}", userId, chatId);
        return chatService.getMessageHistory(userId, chatId)
                .stream()
                .map(MessageVO::new)
                .collect(Collectors.toList());
    }

    /**
     * 根据聊天 id 删除会话
     *
     * @param chatId 聊天 id
     * @param userId 用户 id
     * @return 删除结果
     */
    @RequestMapping("/deleteChat")
    public Boolean deleteByChatId(String chatId, @RequestParam(defaultValue = "1") Long userId) {
        log.info("删除会话, userId: {}, chatId:{}", userId, chatId);
        String conversationId = buildConversationId(userId, chatId);
        try {
            chatService.deleteByChatId(userId, chatId);
            chatMemory.clear(conversationId);
        } catch (Exception e) {
            log.error("删除会话失败, chatId: {}, userId: {}", chatId, userId, e);
            return false;
        }
        return true;
    }

    /**
     * 把已持久化的历史记录同步到 Spring AI 内存中
     *
     * @param userId 用户 id
     * @param chatId 聊天 id
     */
    private void syncHistoryToMemory(Long userId, String chatId, String conversationId) {
        List<Message> currentMessages = chatMemory.get(conversationId);
        if (currentMessages != null && !currentMessages.isEmpty()) {
            return;
        }

        List<ChatMessageDTO> history = chatService.getMessageHistory(userId, chatId);
        if (history.isEmpty()) {
            return;
        }

        List<Message> messages = history.stream()
                .map(this::buildMessage)
                .collect(Collectors.toList());
        chatMemory.add(conversationId, messages);
    }

    /**
     * 持久化消息转 Spring AI Message
     *
     * @param chatMessageDTO 历史消息
     * @return Message
     */
    private Message buildMessage(ChatMessageDTO chatMessageDTO) {
        if ("assistant".equals(chatMessageDTO.getRole())) {
            return new AssistantMessage(chatMessageDTO.getContent());
        }
        List<String> urls = chatMessageDTO.getMediaUrls();
        if (urls != null && !urls.isEmpty()) {
            List<Media> mediaList = new ArrayList<>();
            for (String url : urls) {
                if (url == null || url.isBlank()) {
                    continue;
                }
                String trimmed = url.trim();
                try {
                    mediaList.add(new Media(mimeTypeForImageUrl(trimmed), new URI(trimmed).toURL().toURI()));
                } catch (Exception e) {
                    log.warn("历史消息中的媒体地址无效，已跳过: {}", trimmed, e);
                }
            }
            if (!mediaList.isEmpty()) {
                return UserMessage.builder()
                        .text(chatMessageDTO.getContent())
                        .media(mediaList)
                        .build();
            }
        }
        return new UserMessage(chatMessageDTO.getContent());
    }
}
