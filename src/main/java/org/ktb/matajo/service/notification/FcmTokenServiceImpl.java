package org.ktb.matajo.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM 토큰 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {

    private final UserRepository userRepository;

    /**
     * 사용자의 FCM 토큰 업데이트
     *
     * @param userId 사용자 ID
     * @param fcmToken FCM 토큰
     */
    @Override
    @Transactional
    public void updateUserFcmToken(Long userId, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.error("FCM 토큰이 비어 있습니다: userId={}", userId);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        user.updateFcmToken(fcmToken);
        userRepository.save(user);
        log.info("FCM 토큰 업데이트 완료: userId={}", userId);
    }

    /**
     * 사용자의 FCM 토큰 제거 (로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     */
    @Override
    @Transactional
    public void removeUserFcmToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        user.updateFcmToken(null);
        userRepository.save(user);
        log.info("FCM 토큰 삭제 완료: userId={}", userId);
    }
}