package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "채팅방", description = "채팅방 관련 API")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @Operation(summary = "채팅방 생성", description = "게시글 ID를 기반으로 새로운 채팅방을 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (보관인이 자신의 게시글에 채팅방 생성 시)"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<ChatRoomCreateResponseDto>> createChatRoom(
            @Parameter(description = "채팅방 생성 요청 정보", required = true)
            @Valid @RequestBody ChatRoomCreateRequestDto chatRoomRequestDto) {

        log.info("채팅방 생성: postId={}", chatRoomRequestDto.getPostId());

        Long userId = SecurityUtil.getCurrentUserId();

        ChatRoomCreateResponseDto chatRoom = chatRoomService.createChatRoom(chatRoomRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("create_chat_room_success", chatRoom));
    }

    // 채팅 리스트 조회
    @Operation(summary = "내 채팅방 목록 조회", description = "현재 로그인한 사용자의 채팅방 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(summary = "채팅방 상세 정보 조회", description = "특정 채팅방의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 상세 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "채팅방 없음")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<CommonResponse<ChatRoomDetailResponseDto>> getChatRoomDetail(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable Long roomId) {

        Long userId = SecurityUtil.getCurrentUserId();

        log.info("채팅방 상세 조회: roomId={}, userId={}", roomId, userId);

        ChatRoomDetailResponseDto chatRoomDetail = chatRoomService.getChatRoomDetail(userId, roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_chat_room_detail_success", chatRoomDetail));
    }

    // 채팅방 나가기
    @Operation(summary = "채팅방 나가기", description = "특정 채팅방에서 나갑니다 (보관인은 나갈 수 없음)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 나가기 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "채팅방 나가기 권한 없음 (보관인)"),
            @ApiResponse(responseCode = "404", description = "채팅방 없음"),
            @ApiResponse(responseCode = "409", description = "이미 나간 채팅방")
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<CommonResponse<Void>> leaveChatRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable Long roomId) {

        Long userId = SecurityUtil.getCurrentUserId();

        chatRoomService.leaveChatRoom(userId, roomId);
        log.info("채팅방 나가기: userId={}, roomId={}", userId, roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("delete_chat_room_success", null));
    }
}