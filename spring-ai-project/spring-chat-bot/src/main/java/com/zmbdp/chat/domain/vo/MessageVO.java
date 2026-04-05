package com.zmbdp.chat.domain.vo;

import com.zmbdp.chat.domain.dto.ChatMessageDTO;
import lombok.Data;

import java.util.List;

@Data
public class MessageVO {

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 用户消息中的图片等媒体 URL（若有）
     */
    private List<String> mediaUrls;

    public MessageVO(ChatMessageDTO chatMessageDTO) {
        this.role = chatMessageDTO.getRole();
        this.content = chatMessageDTO.getContent();
        this.mediaUrls = chatMessageDTO.getMediaUrls();
    }
}