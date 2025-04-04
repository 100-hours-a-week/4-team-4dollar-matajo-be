package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ktb.matajo.entity.common.BaseEntity;
import org.ktb.matajo.util.UserTypeConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long kakaoId;

    private String username;
    private String phoneNumber;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Convert(converter = UserTypeConverter.class)
    private UserType role;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean keeperAgreement;

    private LocalDateTime deletedAt;

    // FCM 토큰을 위한 새 필드
    @Column(length = 512)
    private String fcmToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChatRoom> chatRoomList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChatUser> chatUserList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Post> postList = new ArrayList<>();

    // 커맨드 메서드들
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void promoteToKeeper() {
        this.role = UserType.KEEPER;
        this.keeperAgreement = true;
    }

    // FCM 토큰을 업데이트하는 새 메서드
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}