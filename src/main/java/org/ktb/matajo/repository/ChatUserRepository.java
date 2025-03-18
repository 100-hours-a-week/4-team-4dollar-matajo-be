package org.ktb.matajo.repository;

import org.ktb.matajo.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    // 채팅 리스트 조회
    List<ChatUser> findByUserIdAndActiveStatusIsTrue(Long userId);
}
