package org.ktb.matajo.service.chat;

import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    
    // 채팅방 ID를 키로, 활성 사용자 ID 집합을 값으로 하는 동시성 지원 Map
    private final ConcurrentHashMap<Long, Set<Long>> roomToActiveUsers = new ConcurrentHashMap<>();

    // 메모리 이슈 방지를 위한 최대 추적 채팅방 수
    private static final int MAX_TRACKED_ROOMS = 10000;

    @Override
    public void userJoinedRoom(Long roomId, Long userId) {

        validateRoomId(roomId);
        validateUserId(userId);

        // 메모리 한계 확인 (DoS 방지)
        if (roomToActiveUsers.size() >= MAX_TRACKED_ROOMS && !roomToActiveUsers.containsKey(roomId)) {
            log.error("최대 추적 가능한 채팅방 수({})에 도달했습니다. 새 채팅방 {}을 추적할 수 없습니다.",
                    MAX_TRACKED_ROOMS, roomId);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        try {
            // computeIfAbsent: 해당 roomId에 대한 Set이 없으면 새로 생성
            Set<Long> activeUsers = roomToActiveUsers.computeIfAbsent(roomId, k ->
                    Collections.newSetFromMap(new ConcurrentHashMap<>()));

            activeUsers.add(userId);
            log.debug("사용자 입장: roomId={}, userId={}, 현재 인원={}",
                    roomId, userId, activeUsers.size());
        } catch (Exception e) {
            log.error("사용자 입장 처리 중 오류 발생: roomId={}, userId={}, 오류={}",
                    roomId, userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public void userLeftRoom(Long roomId, Long userId) {

        validateRoomId(roomId);
        validateUserId(userId);

        try {
            if (roomToActiveUsers.containsKey(roomId)) {
                Set<Long> activeUsers = roomToActiveUsers.get(roomId);
                boolean removed = activeUsers.remove(userId);

                if (!removed) {
                    log.debug("사용자 {}는 채팅방 {} 활성 사용자 목록에 없습니다", userId, roomId);
                }

                // 만약 채팅방에 남은 사용자가 없다면 Map에서 해당 항목 제거 (메모리 관리)
                if (activeUsers.isEmpty()) {
                    roomToActiveUsers.remove(roomId);
                    log.debug("빈 채팅방 제거: roomId={}", roomId);
                }

                log.debug("사용자 퇴장: roomId={}, userId={}, 남은 인원={}",
                        roomId, userId, activeUsers.size());
            } else {
                log.debug("사용자 {} 퇴장 시 채팅방 {}을 찾을 수 없거나 이미 비어 있습니다", userId, roomId);
            }
        } catch (Exception e) {
            log.error("사용자 퇴장 처리 중 오류 발생: roomId={}, userId={}, 오류={}",
                    roomId, userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public Set<Long> getActiveUsersInRoom(Long roomId) {

        validateRoomId(roomId);

        try {
            // 해당 roomId에 대한 Set이 없으면 빈 Set 반환
            Set<Long> result = roomToActiveUsers.getOrDefault(roomId, Collections.emptySet());

            // 외부 수정 방지를 위한 방어적 복사
            Set<Long> defensiveCopy = Collections.unmodifiableSet(new HashSet<>(result));

            log.debug("활성 사용자 조회: roomId={}, 인원={}", roomId, result.size());
            return defensiveCopy;
        } catch (Exception e) {
            log.error("활성 사용자 조회 중 오류 발생: roomId={}, 오류={}",
                    roomId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    // 채팅방 ID 유효성 검사
    private void validateRoomId(Long roomId) {
        if (roomId == null) {
            log.warn("roomId가 null입니다. 사용자 입장 처리를 건너뜁니다.");
            throw new BusinessException(ErrorCode.INVALID_CHAT_ROOM_ID);
        }

        if (roomId <= 0) {
            log.warn("roomId가 유효하지 않습니다({}). 양수여야 합니다.", roomId);
            throw new BusinessException(ErrorCode.INVALID_CHAT_ROOM_ID);
        }
    }

    // 사용자 ID 유효성 검사
    private void validateUserId(Long userId) {
        if (userId == null) {
            log.warn("userId가 null입니다. 사용자 입장 처리를 건너뜁니다.");
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }

        if (userId <= 0) {
            log.warn("userId가 유효하지 않습니다({}). 양수여야 합니다.", userId);
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }
    }

}