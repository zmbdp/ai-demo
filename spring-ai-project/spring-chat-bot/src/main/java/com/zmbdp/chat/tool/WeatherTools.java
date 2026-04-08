package com.zmbdp.chat.tool;

import org.springframework.ai.tool.annotation.Tool;

public class WeatherTools {

    @Tool(description = "Get the current weather in the given city")
    String getCurrentWeatherByCityName(String cityName) {
        return switch (cityName) {
            case "北京" -> "北京今天天气: 晴空万里";
            case "上海" -> "上海今天天气: 电闪雷鸣";
            case "广州" -> "广州今天天气: 细雨蒙蒙";
            default -> "没有该城市的天气信息";
        };
    }
}
