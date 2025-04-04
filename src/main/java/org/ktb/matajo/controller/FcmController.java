package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.notification.FcmTokenRefreshRequestDto;
import org.ktb.matajo.dto.notification.FcmTokenRequestDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.notification.FcmTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FCM", description = "Firebase Cloud Messaging 관련 API")
public class FcmController {

    private final FcmTokenService fcmTokenService;

    @Operation(summary = "FCM 토큰 등록/갱신", description = "사용자의 FCM 토큰을 등록하거나 갱신합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 등록/갱신 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/token")
    public ResponseEntity<CommonResponse<Void>> registerToken(@RequestBody FcmTokenRequestDto requestDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("FCM 토큰 등록 요청: userId={}", userId);
        
        fcmTokenService.updateUserFcmToken(userId, requestDto.getToken());
        
        return ResponseEntity.ok(CommonResponse.success("fcm_token_updated", null));
    }
    
    @Operation(summary = "FCM 토큰 갱신", description = "Firebase에서 자동으로 갱신된 FCM 토큰을 서버에 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<CommonResponse<Void>> refreshToken(@RequestBody FcmTokenRefreshRequestDto requestDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("FCM 토큰 갱신 요청: userId={}", userId);
        
        // 기존 토큰 검증 후 새 토큰으로 업데이트
        fcmTokenService.updateUserFcmToken(userId, requestDto.getNewToken());
        
        return ResponseEntity.ok(CommonResponse.success("fcm_token_refreshed", null));
    }
    
    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다(로그아웃 시).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/token")
    public ResponseEntity<CommonResponse<Void>> removeToken() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("FCM 토큰 삭제 요청: userId={}", userId);
        
        fcmTokenService.removeUserFcmToken(userId);
        
        return ResponseEntity.ok(CommonResponse.success("fcm_token_removed", null));
    }
}