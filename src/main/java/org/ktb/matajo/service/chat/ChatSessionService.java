package org.ktb.matajo.service.chat;

import java.util.Set;

public interface ChatSessionService {
    /**
     * 사용자가 채팅방에 입장했을 때 호출
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void userJoinedRoom(Long roomId, Long userId);
    
    /**
     * 사용자가 채팅방에서 나갔을 때 호출
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void userLeftRoom(Long roomId, Long userId);
    
    /**
     * 특정 채팅방의 현재 활성 사용자 목록 조회
     * @param roomId 채팅방 ID
     * @return 활성 사용자 ID 집합
     */
    Set<Long> getActiveUsersInRoom(Long roomId);
}