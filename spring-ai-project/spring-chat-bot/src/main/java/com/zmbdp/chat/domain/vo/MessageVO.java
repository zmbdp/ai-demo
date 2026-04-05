package com.zmbdp.chat.domain.vo;

import com.zmbdp.chat.domain.dto.ChatMessageDTO;
import lombok.Data;

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

    public MessageVO(ChatMessageDTO chatMessageDTO) {
        this.role = chatMessageDTO.getRole();
        this.content = chatMessageDTO.getContent();
    }
}