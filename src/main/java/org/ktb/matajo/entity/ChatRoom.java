package org.ktb.matajo.entity;

import java.util.ArrayList;
import java.util.List;

import org.ktb.matajo.entity.common.BaseEntity;

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
      @Index(name = "idx_chat_room_user_id", columnList = "user_id"),
      @Index(name = "idx_chat_room_post_id", columnList = "post_id")
    })
public class ChatRoom extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "post_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false)
  private Post post;

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
  private List<ChatMessage> chatMessageList = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
  private List<ChatUser> chatUserList = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
  private List<TradeInfo> tradeInfoList = new ArrayList<>();
}
