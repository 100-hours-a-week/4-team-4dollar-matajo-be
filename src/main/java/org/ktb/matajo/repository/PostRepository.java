package org.ktb.matajo.repository;

import java.util.List;

import org.ktb.matajo.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL AND p.hiddenStatus = false ORDER BY p.createdAt DESC")
    List<Post> findAllActivePostsOrderByCreatedAtDesc(Pageable pageable);

    // location_info_id로 게시글 직접 조회 (단일 쿼리로 처리)
    @Query("SELECT p FROM Post p JOIN p.address a WHERE a.locationInfo.id = :locationInfoId AND p.deletedAt IS NULL AND p.hiddenStatus = false ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByLocationInfoId(@Param("locationInfoId") Long locationInfoId);
}
