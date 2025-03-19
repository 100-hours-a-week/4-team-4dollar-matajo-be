package org.ktb.matajo.repository;

import org.ktb.matajo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
    Optional<User> findTopByOrderByIdAsc(); // JWT 인증이 없으므로 첫 번째 유저 조회
}
