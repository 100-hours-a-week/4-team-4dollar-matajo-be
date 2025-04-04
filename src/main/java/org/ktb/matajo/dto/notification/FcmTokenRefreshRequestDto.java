package org.ktb.matajo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FCM 토큰 갱신 요청 DTO")
public class FcmTokenRefreshRequestDto {
    @Schema(description = "기존 FCM 토큰", example = "expired_fcm_token...")
    private String oldToken;
    
    @Schema(description = "새 FCM 토큰", example = "new_fcm_token...")
    private String newToken;
}