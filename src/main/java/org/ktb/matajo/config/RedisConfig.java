package org.ktb.matajo.config;

import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 문자열 키와 ChatMessageResponseDto 값을 저장하는 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, ChatMessageResponseDto> chatMessageRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, ChatMessageResponseDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 키는 문자열, 값은 ChatMessageResponseDto를 JSON으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageResponseDto.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageResponseDto.class));

        return template;
    }

    /**
     * 일반적인 문자열 키와 객체 값을 저장하는 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 문자열 키, 값은 범용 JSON 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }

    /**
     * Redis pub/sub을 위한 리스너 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}