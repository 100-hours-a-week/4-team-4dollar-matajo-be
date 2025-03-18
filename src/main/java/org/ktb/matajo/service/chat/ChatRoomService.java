package org.ktb.matajo.service.chat;

import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;

public interface ChatService {
    // 채팅방 생성
    ChatRoomCreateResponseDto createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, Long userId);
}
