package org.ktb.matajo.service.notification;

/**
 * FCM 토큰 관리를 위한 서비스 인터페이스
 */
public interface FcmTokenService {

    /**
     * 사용자의 FCM 토큰을 업데이트합니다
     *
     * @param userId 사용자 ID
     * @param fcmToken FCM 토큰
     */
    void updateUserFcmToken(Long userId, String fcmToken);

    /**
     * 사용자의 FCM 토큰을 제거합니다 (로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     */
    void removeUserFcmToken(Long userId);
}