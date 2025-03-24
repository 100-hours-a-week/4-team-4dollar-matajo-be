package org.ktb.matajo.service.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    
    // 채팅방 ID를 키로, 활성 사용자 ID 집합을 값으로 하는 동시성 지원 Map
    private final ConcurrentHashMap<Long, Set<Long>> roomToActiveUsers = new ConcurrentHashMap<>();
    
    @Override
    public void userJoinedRoom(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            log.warn("roomId 또는 userId가 null입니다. 사용자 입장 처리를 건너뜁니다.");
            return;
        }
        
        // computeIfAbsent: 해당 roomId에 대한 Set이 없으면 새로 생성
        Set<Long> activeUsers = roomToActiveUsers.computeIfAbsent(roomId, k -> 
            Collections.newSetFromMap(new ConcurrentHashMap<>()));
            
        activeUsers.add(userId);
        log.debug("사용자 입장: roomId={}, userId={}, 현재 인원={}", roomId, userId, activeUsers.size());
    }
    
    @Override
    public void userLeftRoom(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            log.warn("roomId 또는 userId가 null입니다. 사용자 퇴장 처리를 건너뜁니다.");
            return;
        }
        
        if (roomToActiveUsers.containsKey(roomId)) {
            Set<Long> activeUsers = roomToActiveUsers.get(roomId);
            activeUsers.remove(userId);
            
            // 만약 채팅방에 남은 사용자가 없다면 Map에서 해당 항목 제거 (메모리 관리)
            if (activeUsers.isEmpty()) {
                roomToActiveUsers.remove(roomId);
                log.debug("빈 채팅방 제거: roomId={}", roomId);
            }
            
            log.debug("사용자 퇴장: roomId={}, userId={}, 남은 인원={}", 
                    roomId, userId, activeUsers.size());
        }
    }
    
    @Override
    public Set<Long> getActiveUsersInRoom(Long roomId) {
        if (roomId == null) {
            log.warn("roomId가 null입니다. 빈 Set을 반환합니다.");
            return Collections.emptySet();
        }
        
        // 해당 roomId에 대한 Set이 없으면 빈 Set 반환
        Set<Long> result = roomToActiveUsers.getOrDefault(roomId, Collections.emptySet());
        log.debug("활성 사용자 조회: roomId={}, 인원={}", roomId, result.size());
        return result;
    }
}