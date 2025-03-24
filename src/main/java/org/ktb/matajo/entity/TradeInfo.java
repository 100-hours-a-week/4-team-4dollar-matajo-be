package org.ktb.matajo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(
    indexes = {
      @Index(name = "idx_trade_info_room_id", columnList = "room_id"),
    })
public class TradeInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(nullable = false, length = 50)
  private String productName;

  @Column(nullable = false, length = 20)
  private String category;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime tradeDate;

    @Column(nullable = false)
    private LocalDateTime startDate;

  @Column(nullable = false)
  private int storagePeriod;

  @Column(nullable = false)
  private int tradePrice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "room_id",
      nullable = false,
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private ChatRoom chatRoom;
}
