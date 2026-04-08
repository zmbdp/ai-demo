package com.zmbdp.chat.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTools {

    @Tool(description = "获取用户时区中的当前日期和时间")
    public String getCurrentDateTime(ToolContext context) {
        System.out.println("chatId====>" + context.getContext().get("chatId"));
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
//        return "2015-01-01 12:00";
    }

//    @Tool(description = "Get the current date and time in the user's timezone")
//    String getCurrentDateTime(String time) {
//        return "2015-01-01 12:00";
//    }

    @Tool(description = "Set a user alarm for the given time")
    public void setAlarm(@ToolParam(description = "Time in ISO-8601 format") String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}