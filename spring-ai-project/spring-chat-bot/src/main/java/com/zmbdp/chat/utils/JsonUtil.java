package com.zmbdp.chat.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 生成无参私有的构造方法，避免外部通过 new 创建对象
public class JsonUtil {

    /**
     * 创建一个 ObjectMapper 对象，这个对象会根据我们后续的配置，进行 json 转换
     */
    private static final ObjectMapper OBJECT_MAPPER;

    /**
     * 静态代码块，初始化 ObjectMapper 对象
     */
    static {
        OBJECT_MAPPER =
                JsonMapper.builder()
                        // 在反序列化时，如果 json 里面有个属性 class 里没有，默认会抛异常，false 就是不让他抛异常，给忽略掉
                        // 比如 json {name: zhangsan, age: 20} 转换成 bloom {name, id} 对象，默认是会抛出异常的，这就是给他忽略掉
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        // 在序列化时，默认会给日期属性变成时间戳，false 就是这么做，按照后续配置去转换
                        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        // 在序列化时，Java对象中没有任何属性（不是没有属性值），默认情况下 Jackson 可能会抛出异常。设置
                        // 此项为 false 后，允许这种情况，直接就返回一个 {}
                        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                        // 在反序列化时，如果 JSON 数据中指定的类型信息与期望的 Java 类型层次结构不匹配（例如类型
                        // 标识错误等情况），默认会抛出异常。将这个配置设为 false，可以放宽这种限制，使得在遇到类
                        // 型不太准确但仍有可能处理的情况下，尝试继续进行反序列化而不是直接失败，提高对可能存在错
                        // 误类型标识的 JSON 数据的容错性。
                        .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                        // 在序列化时，会把日期键（比如 Map 类型的）转换成时间戳，设置成 false 就按照我们后续配置进行转换
                        .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
                        // Jackson 支持通过在 Java 类的属性或方法上添加各种注解来定制序列化和反序列化行为。设置为 false 就不让他生效
                        .configure(MapperFeature.USE_ANNOTATIONS, false)
                        // 这是序列化 LocalDateTIme 和 LocalDate 属性的必要配置， 默认是不支持转换这种类型的
                        .addModule(new JavaTimeModule())
                        // 对 Date 类型的日期格式都统一为以下的样式: yyyy-MM-dd HH:mm:ss
                        .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                        // 对 LocalDateTIme 和 LocalDate 类型起作用的
                        .addModule(new SimpleModule()
                                // 序列时起作用
                                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                // 反序列时起作用
                                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        )
                        // 只针对 非空 的值进行序列化
                        .serializationInclusion(JsonInclude.Include.NON_NULL)
                        .build();
    }

    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty() || clazz == null) {
            return null;
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.warn("JsonUtil.jsonToList error: {}", e.getMessage());
            return null;
        }
    }

    public static <T> String classToJson(T clazz) {
        if (clazz == null || clazz instanceof String) {
            return (String) clazz;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(clazz);
        } catch (JsonProcessingException e) {
            log.warn("JsonUtil.classToJson Class to JSON error: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}