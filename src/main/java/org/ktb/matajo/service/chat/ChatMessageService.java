package org.ktb.matajo.service.chat;

import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;

import java.util.List;

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
     * @return 메시지 목록
     */
    List<ChatMessageResponseDto> getChatMessages(Long roomId);

    /**
     * 메시지 읽음 상태 업데이트
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void markMessagesAsRead(Long roomId, Long userId);
}