package org.ktb.matajo.repository;

import org.ktb.matajo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 기존에 채팅방이 있는지 확인
    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.chatUserList cu " +
            "WHERE cu.user.id = :userId " +
            "AND cr.post.id = :postId " +
            "AND cu.activeStatus = true"
    )
    Optional<ChatRoom> findExistingChatRoom(Long userId, Long postId);

}
