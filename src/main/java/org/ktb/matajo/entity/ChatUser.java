package org.ktb.matajo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(
    indexes = {
      @Index(name = "idx_chat_user_room_id", columnList = "room_id"),
      @Index(name = "idx_chat_user_user_id", columnList = "user_id")
    })
public class ChatUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "room_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false)
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false)
  private User user;

  @Column(nullable = false, columnDefinition = "TINYINT(1)")
  private boolean activeStatus;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime joinedAt;

  private LocalDateTime leftAt;

  // 채팅방 나가기
  public void leave() {
    this.activeStatus = false;
    this.leftAt = LocalDateTime.now();
  }

  // 채팅방 다시 들어오기
  public void rejoin() {
    this.activeStatus = true;
    this.leftAt = null;
  }
}
