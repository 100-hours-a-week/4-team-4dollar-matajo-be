package org.ktb.matajo.repository;

import org.ktb.matajo.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방의 메시지 목록 조회 (페이지네이션 적용)
     */
    List<ChatMessage> findByChatRoomId(Long roomId);

    /**
     * 읽지 않은 메시지 중 특정 사용자가 보내지 않은 메시지만 조회
     */
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId " +
            "AND m.user.id != :userId " +
            "AND m.readStatus = false")
    List<ChatMessage> findUnreadMessagesForUser(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 채팅방의 읽지 않은 메시지 수 조회
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId " +
            "AND m.user.id != :userId " +
            "AND m.readStatus = false")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
}