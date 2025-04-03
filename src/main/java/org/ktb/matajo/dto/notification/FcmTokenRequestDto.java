package org.ktb.matajo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FCM 토큰 요청 DTO")
public class FcmTokenRequestDto {
    @Schema(description = "FCM 토큰", example = "eIEkdl82hfOWnfis72...")
    private String token;
}