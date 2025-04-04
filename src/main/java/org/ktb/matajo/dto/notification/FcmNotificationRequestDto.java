package org.ktb.matajo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FCM 알림 요청 DTO")
public class FcmNotificationRequestDto {
    @Schema(description = "수신자 FCM 토큰")
    private String fcmToken;
    
    @Schema(description = "알림 제목")
    private String title;
    
    @Schema(description = "알림 내용")
    private String body;
    
    @Schema(description = "추가 데이터 (키-값 쌍)")
    private Map<String, String> data;
}