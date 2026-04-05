package com.zmbdp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.chat.domain.dto.ChatMessageDTO;
import com.zmbdp.chat.domain.dto.ChatSessionDTO;
import com.zmbdp.chat.domain.entity.ChatMessage;
import com.zmbdp.chat.domain.entity.ChatSession;
import com.zmbdp.chat.mapper.ChatMessageMapper;
import com.zmbdp.chat.mapper.ChatSessionMapper;
import com.zmbdp.chat.service.IChatService;
import com.zmbdp.chat.service.IChatSessionService;
import com.zmbdp.chat.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements IChatService {

    /**
     * 聊天表 Mapper
     */
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    /**
     * 聊天消息表 Mapper
     */
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 聊天会话服务
     */
    @Autowired
    private IChatSessionService chatSessionService;

    /**
     * 保存聊天记录
     * 如果是新聊天, 则新增
     * 如果聊天已经存在但标题为空, 则补充标题
     * 如果聊天标题已存在, 则不覆盖
     *
     * @param chatId 聊天 id
     * @param title  聊天标题
     */
    @Override
    public void save(String chatId, String title) {
        Long userId = 34234234324234L;
        ChatSessionDTO chatSessionDTO = chatSessionService.getByChatId(chatId, userId);
        // 如果已存在, 并且还有标题啥的, 就直接返回
        if (chatSessionDTO != null && chatSessionDTO.getTitle() != null && !chatSessionDTO.getTitle().isBlank()) {
            return;
        }

        String normalizedTitle = normalizeTitle(title);
        ChatSessionDTO cachedChatSession = new ChatSessionDTO();
        cachedChatSession.setId(chatId);
        cachedChatSession.setUserId(userId);
        cachedChatSession.setTitle(normalizedTitle);
        if (!chatSessionService.insertOrUpdate(cachedChatSession)) {
            throw new RuntimeException("聊天消息持久化失败！");
        }
    }

    /**
     * 保存一条聊天消息
     *
     * @param chatId  聊天 id
     * @param role    消息角色
     * @param content 消息内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(String chatId, String role, String content, List<String> mediaUrls) {
        Long userId = 34234234324234L;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setUserId(userId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            chatMessage.setMediaUrls(JsonUtil.classToJson(mediaUrls));
        }
        chatMessageMapper.insert(chatMessage);
    }

    /**
     * 删除聊天记录
     *
     * @param chatId 聊天 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByChatId(String chatId) {
        Long userId = 34234234324234L;
        chatSessionMapper.delete(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, chatId)
                .eq(ChatSession::getUserId, userId));
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getChatId, chatId)
                .eq(ChatMessage::getUserId, userId));
    }

    /**
     * 获取所有聊天记录
     *
     * @return 聊天记录
     */
    @Override
    public List<ChatSessionDTO> getSessionHistory() {
        Long userId = 34234234324234L;
        return buildChatInfoList(userId);
    }

    /**
     * 获取聊天历史记录
     *
     * @param chatId 聊天 id
     * @return 历史记录
     */
    @Override
    public List<ChatMessageDTO> getMessageHistory(String chatId) {
        Long userId = 34234234324234L;
        return chatMessageMapper.selectList(
                        new LambdaQueryWrapper<ChatMessage>()
                                .eq(ChatMessage::getChatId, chatId)
                                .eq(ChatMessage::getUserId, userId)
                                .orderByAsc(ChatMessage::getId)
                ).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 构建聊天列表
     *
     * @param userId 用户 id
     * @return 聊天列表
     */
    private List<ChatSessionDTO> buildChatInfoList(Long userId) {
        return chatSessionMapper.selectList(
                        new LambdaQueryWrapper<ChatSession>()
                                .eq(ChatSession::getUserId, userId)
                                .orderByDesc(ChatSession::getId)
                ).stream()
                .map(chatSession -> new ChatSessionDTO(chatSession.getId(), userId, chatSession.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 规范化标题
     *
     * @param title 原始标题
     * @return 标题
     */
    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "无标题";
        }
        return title.length() > 64 ? title.substring(0, 64) : title;
    }
}