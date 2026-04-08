package com.zmbdp.chat.tool;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageTools {

    @Autowired
    private DashScopeImageModel imageModel;

    @Autowired
    private DashScopeImageOptions imageOptions;

    @Tool(description = "根据提示词生成图片并返回URL")
    public String getImage(@ToolParam(description = "用户用来生成图片的提示词，例如：一只可爱的小猫") String prompt) {
        for (; true;) {
            try {
                ImageResponse imageResponse = imageModel.call(new ImagePrompt(prompt, imageOptions));
                System.out.println(imageResponse.getResult().getOutput().getUrl());
                return imageResponse.getResult().getOutput().getUrl();
            } catch (RuntimeException e) {
                if (e.getMessage() == null || !e.getMessage().contains("Image generation still pending")) {
                    throw e;
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待图片生成时线程被中断", ex);
                }
            }
        }
    }
}
