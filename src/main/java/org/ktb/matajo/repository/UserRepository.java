package org.ktb.matajo.repository;

import org.ktb.matajo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);
    boolean existsByNickname(String nickname);
}
