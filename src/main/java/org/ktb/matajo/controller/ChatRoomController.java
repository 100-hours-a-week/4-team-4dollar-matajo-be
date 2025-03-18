package org.ktb.matajo.controller;

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

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<CommonResponse<ChatRoomCreateResponseDto>> createChatRoom(
            @RequestBody ChatRoomCreateRequestDto chatRoomRequestDto) {

        log.info("채팅방 생성: postId={}", chatRoomRequestDto.getPostId());

        // userId 로직은 나중에 로그인 구현되면 수정할 부분!
        ChatRoomCreateResponseDto chatRoom = chatRoomService.createChatRoom(chatRoomRequestDto, 1L);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("create_chat_success", chatRoom));
    }

    // 채팅 리스트 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<ChatRoomResponseDto>>> getChatRoom() {

        List<ChatRoomResponseDto> myChatRooms = chatRoomService.getMyChatRooms(1L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_chat_list_success", myChatRooms));
    }
}
