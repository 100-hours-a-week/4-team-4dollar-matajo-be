package org.ktb.matajo.repository;

import java.util.List;
import java.util.Optional;

import org.ktb.matajo.entity.ChatRoom;
import org.ktb.matajo.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
  // 사용자 기준 활성화된 채팅 리스트 조회
  List<ChatUser> findByUserIdAndActiveStatusIsTrue(Long userId);

  // 해당 채팅방의 사용자 참여 정보 조회
  Optional<ChatUser> findByChatRoomAndUserId(ChatRoom chatRoom, Long userId);

  // 특정 사용자가 특정 채팅방에 활성 상태로 참여 중인지 확인
  boolean existsByUserIdAndChatRoomIdAndActiveStatusIsTrue(Long userId, Long roomId);
}
