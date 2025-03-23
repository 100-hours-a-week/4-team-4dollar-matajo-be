package org.ktb.matajo.controller;

import java.util.List;

import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.chat.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<CommonResponse<ChatRoomCreateResponseDto>> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequestDto chatRoomRequestDto,
            @RequestHeader(value = "userId", required = false) Long userId) {

        log.info("채팅방 생성: postId={}", chatRoomRequestDto.getPostId());
        ChatRoomCreateResponseDto chatRoom = chatRoomService.createChatRoom(chatRoomRequestDto, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("create_chat_room_success", chatRoom));
    }

    // 채팅 리스트 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<ChatRoomResponseDto>>> getChatRoom(
            @RequestHeader(value = "userId", required = false) Long userId) {

        log.info("채팅 리스트 조회: userId={}", userId);
        List<ChatRoomResponseDto> myChatRooms = chatRoomService.getMyChatRooms(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_chat_list_success", myChatRooms));
    }

    // 채팅방 나가기
    @DeleteMapping("/{roomId}")
    public ResponseEntity<CommonResponse<Void>> leaveChatRoom(
            @PathVariable Long roomId,
            @RequestHeader(value = "userId", required = false) Long userId) {

        chatRoomService.leaveChatRoom(userId, roomId);
        log.info("채팅방 나가기: userId={}, roomId={}", userId, roomId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success("delete_chat_room_success", null));
    }
}
