package org.ktb.matajo.service.chat;

import java.util.List;

import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;

public interface ChatMessageService {
  /**
   * 채팅 메시지 저장
   *
   * @param roomId 채팅방 ID
   * @param messageDto 메시지 정보
   * @return 저장된 메시지 응답 DTO
   */
  ChatMessageResponseDto saveMessage(Long roomId, ChatMessageRequestDto messageDto);

  /**
   * 채팅방의 메시지 목록 조회
   *
   * @param roomId 채팅방 ID
   * @param page 페이지 번호
   * @param size 페이지 사이즈
   * @return 메시지 목록
   */
  List<ChatMessageResponseDto> getChatMessages(Long roomId, int page, int size);

  /**
   * 메시지 읽음 상태 업데이트
   *
   * @param roomId 채팅방 ID
   * @param userId 사용자 ID
   */
  void markMessagesAsRead(Long roomId, Long userId);
}
