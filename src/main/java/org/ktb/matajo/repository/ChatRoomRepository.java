package org.ktb.matajo.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.ktb.matajo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 게시글 ID, 사용자 ID 기준으로 기존 채팅방 조회 - activeStatus 상관없이
    Optional<ChatRoom> findByPostIdAndUserId(Long postId, Long userId);
}
