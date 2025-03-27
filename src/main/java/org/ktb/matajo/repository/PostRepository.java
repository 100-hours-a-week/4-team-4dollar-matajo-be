package org.ktb.matajo.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.ktb.matajo.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL AND p.hiddenStatus = false ORDER BY p.createdAt DESC")
    List<Post> findAllActivePostsOrderByCreatedAtDesc(Pageable pageable);

    // location_info_id로 게시글 직접 조회 (단일 쿼리로 처리)
    @Query("SELECT p FROM Post p " +
           "JOIN p.address a " +
           "WHERE a.locationInfo.id = :locationInfoId " + 
           "AND p.deletedAt IS NULL " +
           "AND p.hiddenStatus = false " +
           "ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByLocationInfoId(@Param("locationInfoId") Long locationInfoId);


    //할인율 상위 2개 조회
    @Query("SELECT p FROM Post p " +
           "JOIN p.address a " +
           "WHERE a.locationInfo.id = :locationInfoId " +
           "AND p.deletedAt IS NULL " +
           "AND p.hiddenStatus = false " +
           "AND p.discountRate > 0 " +
           "ORDER BY p.discountRate DESC " +
           "LIMIT 2")
    List<Post> findTopDiscountedPostsByLocationInfoId(
        @Param("locationInfoId") Long locationInfoId
    );

    // ✅ [추가] 유저 ID로 게시글 조회 (페이지네이션 포함)
    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN p.address a " +
            "WHERE p.user.id = :userId " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
