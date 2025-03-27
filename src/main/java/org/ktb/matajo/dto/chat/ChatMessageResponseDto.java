package org.ktb.matajo.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.ktb.matajo.entity.MessageType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDto {
    @Schema(description = "메시지 고유 ID", example = "1")
    private Long messageId;

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "발신자 ID", example = "2")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;

    @Schema(description = "메시지 타입 (TEXT, IMAGE, SYSTEM)", example = "TEXT")
    private MessageType messageType;

    @Schema(description = "읽음 상태", example = "false")
    private boolean readStatus;

    @Schema(description = "생성 시간", example = "2023-09-15T14:30:45")
    private LocalDateTime createdAt;

    @Schema(description = "발신자 닉네임", example = "사용자1")
    private String senderNickname;
}