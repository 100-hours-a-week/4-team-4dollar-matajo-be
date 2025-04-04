package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.notification.NotificationResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "알림", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "현재 사용자의 읽지 않은 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<NotificationResponseDto>>> getMyNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("알림 목록 조회 요청: userId={}", userId);

        List<NotificationResponseDto> notifications = notificationService.getNotificationsForUser(userId);

        return ResponseEntity.ok(
            CommonResponse.success("notifications_retrieved", notifications)
        );
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 알림을 읽음 상태로 변경합니다.")
    @PutMapping("/read")
    public ResponseEntity<CommonResponse<Void>> markNotificationsAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("알림 읽음 처리 요청: userId={}", userId);

        notificationService.markNotificationsAsRead(userId);

        return ResponseEntity.ok(
            CommonResponse.success("notifications_marked_read", null)
        );
    }
}