package com.zmbdp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.chat.domain.dto.ChatSessionDTO;
import com.zmbdp.chat.domain.entity.ChatSession;
import com.zmbdp.chat.mapper.ChatSessionMapper;
import com.zmbdp.chat.service.IChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 聊天会话服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements IChatSessionService {

    /**
     * 聊天会话 mapper
     */
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    /**
     * 插入或更新消息
     *
     * @param chatSendReqDTO 消息发送参数
     * @return 插入或更新结果
     */
    @Override
    public Boolean insertOrUpdate(ChatSessionDTO chatSendReqDTO) {
        // 先拿到聊天 id 和用户 id，把缓存给删了
        Long userId = chatSendReqDTO.getUserId();
        String chatId = chatSendReqDTO.getId();

        // 先查询，没查到就插入
        ChatSessionDTO chatSessionDTO = getByChatId(chatId, userId);
        ChatSession chatSession = new ChatSession();
        if (chatSessionDTO == null || chatSessionDTO.getId() == null || chatSessionDTO.getUserId() == null) {
            // 说明没查到，就插入
            chatSessionDTO = new ChatSessionDTO();
            chatSessionDTO.setId(chatId);
            chatSessionDTO.setUserId(userId);
            chatSessionDTO.setTitle(chatSendReqDTO.getTitle());
            BeanUtils.copyProperties(chatSessionDTO, chatSession);
            return chatSessionMapper.insert(chatSession) > 0;
        } else if (chatSessionDTO.getTitle() == null || chatSessionDTO.getTitle().isBlank()) {
            // 查到了就看有没有标题，没有标题就更新，有的话就不管了
            chatSessionDTO.setTitle(chatSendReqDTO.getTitle());
            BeanUtils.copyProperties(chatSendReqDTO, chatSession);
            return chatSessionMapper.updateById(chatSession) > 0;
        }
        return true;
    }

    /**
     * 根据 chatId 查询聊天会话
     *
     * @param chatId 聊天 id
     * @param userId 用户 id
     * @return 聊天会话
     */
    @Override
    public ChatSessionDTO getByChatId(String chatId, Long userId) {
        ChatSessionDTO chatSessionDTO = new ChatSessionDTO();
        ChatSession chatSession = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, chatId)
                .eq(ChatSession::getUserId, userId)
                .last("limit 1"));
        if (chatSession != null) {
            BeanUtils.copyProperties(chatSession, chatSessionDTO);
        }
        return chatSessionDTO;
    }
}