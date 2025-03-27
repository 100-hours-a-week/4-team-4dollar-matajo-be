package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.ktb.matajo.entity.MessageType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅 메시지 요청 DTO")
public class ChatMessageRequestDto {

    @Schema(description = "발신자 ID", example = "1", required = true)
    @NotNull(message = "발신자 ID는 필수 항목입니다")
    @Positive(message = "발신자 ID는 양수여야 합니다")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요!", required = true, maxLength = 500)
    @NotBlank(message = "메시지 내용 필수 항목입니다.")
    @Size(min=1, max = 500, message = "메시지 내용은 1자 이 500자 이하여야 합니다")
    private String content;

    @Schema(description = "메시지 타입 (TEXT, IMAGE, SYSTEM)", example = "TEXT", required = true, defaultValue = "TEXT")
    @NotNull(message = "메시지 타입은 필수 항목입니다")
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    // 추가 유효성 검증 메서드
    public boolean isImageTypeWithEmptyContent() {
        return MessageType.IMAGE.equals(this.messageType) &&
                (this.content == null || this.content.trim().isEmpty());
    }

    public boolean isValidImageUrl() {
        if (MessageType.IMAGE.equals(this.messageType)) {
            return content != null && content.startsWith("https://");
        }
        return true; // 이미지 타입이 아니면 검증 통과
    }
}