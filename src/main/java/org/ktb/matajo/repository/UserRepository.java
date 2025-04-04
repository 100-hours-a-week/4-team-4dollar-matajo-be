package org.ktb.matajo.repository;

import org.ktb.matajo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);          // 카카오용
    Optional<User> findTopByOrderByIdAsc();              // 임시 사용자 조회용 (예: JWT 없는 환경)
    boolean existsByNickname(String nickname);           // 닉네임 중복 체크
}
