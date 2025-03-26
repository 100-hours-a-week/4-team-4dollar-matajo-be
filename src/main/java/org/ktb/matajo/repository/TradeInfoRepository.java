package org.ktb.matajo.repository;

import java.util.List;

import org.ktb.matajo.entity.TradeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeInfoRepository extends JpaRepository<TradeInfo, Long> {
  // 채팅방 ID로 거래 정보 조회
  List<TradeInfo> findByChatRoomId(Long chatRoomId);

  // 사용자가 참여한 모든 거래 정보 조회 (최적화된 쿼리)
  @Query(
      "SELECT t FROM TradeInfo t "
          + "JOIN t.chatRoom cr "
          + "JOIN cr.chatUserList cu "
          + "WHERE cu.user.id = :userId AND cu.activeStatus = true "
          + "ORDER BY t.tradeDate DESC")
  List<TradeInfo> findTradeInfoByUserId(@Param("userId") Long userId);
}
