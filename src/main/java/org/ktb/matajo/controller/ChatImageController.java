package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.s3.S3Service;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chats/image")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "채팅 이미지", description = "채팅 이미지 업로드 API")
public class ChatImageController {

    private final S3Service s3Service;

    @Operation(summary = "채팅 이미지 업로드", description = "채팅에서 사용할 이미지를 S3에 업로드합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 형식, 크기 등)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<String>> uploadChatImage(
            @Parameter(description = "업로드할 채팅 이미지 파일", required = true)
            @RequestParam("chatImage") MultipartFile chatImage) {

        log.info("채팅 이미지 업로드 요청: {}", chatImage.getOriginalFilename());

        // S3에 이미지 업로드
        String imageUrl = s3Service.uploadImage(chatImage, "chat");

        log.info("이미지 업로드 완료: {}", imageUrl);

        return ResponseEntity.ok(CommonResponse.success("chat_image_upload_success", imageUrl));
    }
}