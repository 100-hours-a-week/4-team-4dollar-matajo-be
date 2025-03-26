package org.ktb.matajo.service.chat;

import java.util.List;

import org.ktb.matajo.dto.chat.ChatMessageResponseDto;

/** Redis를 활용한 채팅 메시지 캐싱 서비스 인터페이스 */
public interface RedisChatMessageService {

  /**
   * 단일 메시지 캐싱
   *
   * @param roomId 채팅방 ID
   * @param message 캐싱할 메시지
   */
  void cacheMessage(Long roomId, ChatMessageResponseDto message);

  /**
   * 메시지 리스트 캐싱
   *
   * @param roomId 채팅방 ID
   * @param messages 캐싱할 메시지 리스트
   */
  void cacheMessages(Long roomId, List<ChatMessageResponseDto> messages);

  /**
   * 캐시된 메시지 조회
   *
   * @param roomId 채팅방 ID
   * @param limit 조회할 최대 메시지 수
   * @return 캐시된 메시지 리스트
   */
  List<ChatMessageResponseDto> getCachedMessages(Long roomId, int limit);

  /**
   * 채팅방의 캐시 무효화
   *
   * @param roomId 채팅방 ID
   */
  void invalidateCache(Long roomId);
}
