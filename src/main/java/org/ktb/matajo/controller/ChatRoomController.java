package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomDetailResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.chat.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<CommonResponse<ChatRoomCreateResponseDto>> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequestDto chatRoomRequestDto) {

        log.info("채팅방 생성: postId={}", chatRoomRequestDto.getPostId());

        Long userId = SecurityUtil.getCurrentUserId();

        ChatRoomCreateResponseDto chatRoom = chatRoomService.createChatRoom(chatRoomRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("create_chat_room_success", chatRoom));
    }

    // 채팅 리스트 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<ChatRoomResponseDto>>> getChatRoom() {

        Long userId = SecurityUtil.getCurrentUserId();

        log.info("채팅 리스트 조회: userId={}", userId);

        List<ChatRoomResponseDto> myChatRooms = chatRoomService.getMyChatRooms(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_chat_list_success", myChatRooms));
    }

    // 채팅방 상세 정보 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<CommonResponse<ChatRoomDetailResponseDto>> getChatRoomDetail(
            @PathVariable Long roomId) {

        Long userId = SecurityUtil.getCurrentUserId();

        log.info("채팅방 상세 조회: roomId={}, userId={}", roomId, userId);

        ChatRoomDetailResponseDto chatRoomDetail = chatRoomService.getChatRoomDetail(userId, roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_chat_room_detail_success", chatRoomDetail));
    }

    // 채팅방 나가기
    @DeleteMapping("/{roomId}")
    public ResponseEntity<CommonResponse<Void>> leaveChatRoom(@PathVariable Long roomId) {

        Long userId = SecurityUtil.getCurrentUserId();

        chatRoomService.leaveChatRoom(userId, roomId);
        log.info("채팅방 나가기: userId={}, roomId={}", userId, roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("delete_chat_room_success", null));
    }
}
