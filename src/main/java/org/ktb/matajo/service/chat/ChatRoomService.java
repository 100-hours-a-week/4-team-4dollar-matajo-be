package org.ktb.matajo.service.chat;

import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;

import java.util.List;

public interface ChatRoomService {
    // 채팅방 생성
    ChatRoomCreateResponseDto createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, Long userId);

    // 채팅방 목록 조회
    List<ChatRoomResponseDto> getMyChatRooms(Long userId);

    // 채팅방 나가기
    void leaveChatRoom(Long userId, Long roomId);
}
