package org.ktb.matajo.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.ktb.matajo.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findAllActivePostsOrderByCreatedAtDesc(Pageable pageable);

    // 리포지토리에 특화된 메서드 추가
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.address WHERE p.id = :id")
    Optional<Post> findByIdWithAddress(@Param("id") Long id);


    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findActivePostById(@Param("id") Long id);

    // 주소 조회시 sido, sigungu 데이터만 가져옴
    @Query("SELECT p, a.sido, a.sigungu FROM Post p JOIN p.address a WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Object[]> findAllActivePostsWithAddressInfoOrderByCreatedAtDesc(Pageable pageable);
}
