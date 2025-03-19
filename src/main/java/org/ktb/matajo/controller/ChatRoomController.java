package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
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

    // 테스트용 의뢰인 ID (실제 구현 시 제거)
    private static final Long TEST_CLIENT_ID = 1L;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<CommonResponse<ChatRoomCreateResponseDto>> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequestDto chatRoomRequestDto) {

        log.info("채팅방 생성: postId={}", chatRoomRequestDto.getPostId());

        // userId 로직은 나중에 로그인 구현되면 수정할 부분!
        ChatRoomCreateResponseDto chatRoom = chatRoomService.createChatRoom(chatRoomRequestDto, TEST_CLIENT_ID);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("create_chat_room_success", chatRoom));
    }

    // 채팅 리스트 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<ChatRoomResponseDto>>> getChatRoom() {

        log.info("채팅 리스트 조회: userId={}", TEST_CLIENT_ID);

        List<ChatRoomResponseDto> myChatRooms = chatRoomService.getMyChatRooms(TEST_CLIENT_ID);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_chat_list_success", myChatRooms));
    }

    // 채팅방 나가기
    @DeleteMapping("/{roomId}")
    public ResponseEntity<CommonResponse<Void>> leaveChatRoom(@PathVariable Long roomId) {

        chatRoomService.leaveChatRoom(TEST_CLIENT_ID, roomId);
        log.info("채팅방 나가기: userId={}, roomId={}", TEST_CLIENT_ID, roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("delete_chat_room_success", null));
    }
}
