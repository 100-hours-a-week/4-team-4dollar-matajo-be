package org.ktb.matajo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.ktb.matajo.entity.NotificationType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "알림 응답 DTO")
public class NotificationResponseDto {
    @Schema(description = "알림 타입")
    private NotificationType type;

    @Schema(description = "채팅방 ID")
    private Long roomId;

    @Schema(description = "발신자 ID")
    private Long senderId;

    @Schema(description = "발신자 닉네임")
    private String senderNickname;

    @Schema(description = "알림 내용")
    private String content;

    @Schema(description = "알림 시간")
    private LocalDateTime createdAt;
}