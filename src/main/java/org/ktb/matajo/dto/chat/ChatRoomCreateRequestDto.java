package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 생성 요청 DTO") // Swagger 설명은 한글 OK
public class ChatRoomCreateRequestDto {

    @Schema(description = "게시글 ID", example = "1", required = true)
    @NotNull(message = "required_post_id")
    @Positive(message = "positive_post_id")
    private Long postId;
}
