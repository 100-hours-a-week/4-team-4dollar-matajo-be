package org.ktb.matajo.repository;

import org.ktb.matajo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  User findByKakaoId(String kakaoId);
  User findByNickname(String nickname); // 추가된 메서드
}
