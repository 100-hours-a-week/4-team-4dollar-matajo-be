package org.ktb.matajo.service.chat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Redis를 활용한 채팅 메시지 캐싱 서비스 구현체 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatMessageServiceImpl implements RedisChatMessageService {

  private final RedisTemplate<String, Object> redisTemplate;

  // Redis 캐시 키 접두사
  private static final String CHAT_MESSAGES_KEY = "chat:messages:";
  // Redis 캐시 만료 시간 (24시간)
  private static final long CACHE_TTL_HOURS = 24;

  /** 단일 메시지 캐싱 */
  @Override
  public void cacheMessage(Long roomId, ChatMessageResponseDto message) {
    try {
      String cacheKey = CHAT_MESSAGES_KEY + roomId;
      redisTemplate.opsForList().rightPush(cacheKey, message);
      redisTemplate.expire(cacheKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
      log.debug("메시지 캐싱 성공: roomId={}, messageId={}", roomId, message.getMessageId());
    } catch (Exception e) {
      log.error("메시지 캐싱 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  /** 메시지 리스트 캐싱 */
  @Override
  public void cacheMessages(Long roomId, List<ChatMessageResponseDto> messages) {
    try {
      if (messages.isEmpty()) {
        return;
      }

      String cacheKey = CHAT_MESSAGES_KEY + roomId;
      // 기존 캐시 삭제
      redisTemplate.delete(cacheKey);

      // 새로운 데이터 캐싱
      for (ChatMessageResponseDto dto : messages) {
        redisTemplate.opsForList().rightPush(cacheKey, dto);
      }
      redisTemplate.expire(cacheKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
      log.debug("메시지 리스트 캐싱 성공: roomId={}, count={}", roomId, messages.size());
    } catch (Exception e) {
      log.error("메시지 리스트 캐싱 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  /** 캐시된 메시지 조회 */
  @Override
  @SuppressWarnings("unchecked")
  public List<ChatMessageResponseDto> getCachedMessages(Long roomId, int limit) {
    try {
      String cacheKey = CHAT_MESSAGES_KEY + roomId;
      Long size = redisTemplate.opsForList().size(cacheKey);

      if (size == null || size == 0) {
        return Collections.emptyList();
      }

      // 최대 limit 개수만큼 가져오기
      int endIndex = Math.min(limit - 1, size.intValue() - 1);
      List<Object> cachedMessages = redisTemplate.opsForList().range(cacheKey, 0, endIndex);

      if (cachedMessages == null || cachedMessages.isEmpty()) {
        return Collections.emptyList();
      }

      return cachedMessages.stream()
          .map(msg -> (ChatMessageResponseDto) msg)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("캐시된 메시지 조회 중 오류 발생: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /** 채팅방의 캐시 무효화 */
  @Override
  public void invalidateCache(Long roomId) {
    try {
      String cacheKey = CHAT_MESSAGES_KEY + roomId;
      redisTemplate.delete(cacheKey);
      log.debug("채팅방 캐시 무효화 성공: roomId={}", roomId);
    } catch (Exception e) {
      log.error("채팅방 캐시 무효화 중 오류 발생: {}", e.getMessage(), e);
    }
  }
}
