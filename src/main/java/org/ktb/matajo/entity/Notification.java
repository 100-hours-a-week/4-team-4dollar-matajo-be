package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ktb.matajo.entity.common.BaseEntity;
import org.ktb.matajo.util.UserTypeConverter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_read_status", columnList = "read_status")
})
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User receiver;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderNickname;

    @Column(nullable = false)
    private Long chatRoomId; // resourceId 대신 명확하게 chatRoomId로 변경

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean readStatus;

    public void markAsRead() {
        this.readStatus = true;
    }
}