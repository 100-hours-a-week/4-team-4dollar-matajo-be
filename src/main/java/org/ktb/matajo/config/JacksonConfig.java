package org.ktb.matajo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 시간대 설정
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // snake_case to camelCase 설정 추가
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 날짜 포맷 명시적 설정
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"));

        // Asia/Seoul 시간대로 LocalDateTime 직렬화
        LocalDateTimeSerializer seoulTimeSerializer = new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")
                        .withZone(ZoneId.of("Asia/Seoul"))
        );

        javaTimeModule.addSerializer(LocalDateTime.class, seoulTimeSerializer);

        objectMapper.registerModule(javaTimeModule);

        // 타임스탬프로 쓰지 않도록 설정
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}