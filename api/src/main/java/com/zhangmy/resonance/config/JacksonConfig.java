package com.zhangmy.resonance.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 宽松的 LocalDate 反序列化器：
     * 支持 yyyy-MM-dd / yyyy-MM（自动补 1 号）/ yyyy（自动补 1-1）
     */
    static class LenientLocalDateDeserializer extends StdDeserializer<LocalDate> {
        LenientLocalDateDeserializer() {
            super(LocalDate.class);
        }

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null) return null;
            text = text.trim();
            if (text.isEmpty()) return null;
            // 1) yyyy-MM-dd
            try { return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException ignored) {}
            // 2) yyyy-MM
            try { return YearMonth.parse(text, DateTimeFormatter.ofPattern("yyyy-MM")).atDay(1); } catch (DateTimeParseException ignored) {}
            // 3) yyyy
            try { return Year.parse(text).atMonth(1).atDay(1); } catch (DateTimeParseException ignored) {}
            return (LocalDate) ctxt.handleWeirdStringValue(LocalDate.class, text,
                    "不支持的日期格式，请使用 yyyy-MM-dd / yyyy-MM / yyyy");
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            builder.serializers(
                            new LocalDateSerializer(dateFormatter),
                            new LocalDateTimeSerializer(dateTimeFormatter))
                    .deserializers(
                            new LenientLocalDateDeserializer(),
                            new LocalDateTimeDeserializer(dateTimeFormatter));
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).build();
    }
}
