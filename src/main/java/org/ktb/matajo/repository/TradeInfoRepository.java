package org.ktb.matajo.repository;

import org.ktb.matajo.entity.TradeInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeInfoRepository extends JpaRepository<TradeInfo, Long> {
    // 채팅방 ID로 거래 정보 조회
    List<TradeInfo> findByChatRoomId(Long chatRoomId);

    // 사용자가 참여한 모든 거래 정보 조회 (최적화된 쿼리)
    @Query("SELECT t FROM TradeInfo t " +
            "JOIN t.chatRoom cr " +
            "JOIN cr.chatUserList cu " +
            "WHERE cu.user.id = :userId AND cu.activeStatus = true " +
            "ORDER BY t.tradeDate DESC")
    List<TradeInfo> findTradeInfoByUserId(@Param("userId") Long userId);

    // LocationInfo의 id 값을 기준으로 최근 거래 내역 2개를 조회하는 쿼리
    @Query("SELECT t FROM TradeInfo t " +
            "JOIN t.chatRoom c " +
            "JOIN c.post p " +
            "JOIN p.address a " +
            "JOIN a.locationInfo l " +
            "WHERE l.id = :locationId " +
            "ORDER BY t.tradeDate DESC")
    List<TradeInfo> findTop2ByLocationId(@Param("locationId") Long locationId, Pageable pageable);
}
