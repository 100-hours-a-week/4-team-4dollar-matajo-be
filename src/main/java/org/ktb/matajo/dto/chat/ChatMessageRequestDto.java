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
    @NotNull(message = "Sender ID is required.")
    @Positive(message = "Sender ID must be a positive number.")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요!", required = true, maxLength = 500)
    @NotBlank(message = "Message content must not be blank.")
    @Size(min = 1, max = 500, message = "Message content must be between 1 and 500 characters.")
    private String content;

    @Schema(description = "메시지 타입 (TEXT, IMAGE, SYSTEM)", example = "TEXT", required = true, defaultValue = "TEXT")
    @NotNull(message = "Message type is required.")
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    // Additional validation methods
    public boolean isImageTypeWithEmptyContent() {
        return MessageType.IMAGE.equals(this.messageType) &&
                (this.content == null || this.content.trim().isEmpty());
    }

    public boolean isValidImageUrl() {
        if (MessageType.IMAGE.equals(this.messageType)) {
            return content != null && content.startsWith("https://");
        }
        return true;
    }
}
