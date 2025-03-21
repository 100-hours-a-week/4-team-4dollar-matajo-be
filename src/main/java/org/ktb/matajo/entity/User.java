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

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    private String username;

    private String phoneNumber;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Convert(converter = UserTypeConverter.class)
    private UserType role; // 1(USER), 2(KEEPER)

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean keeperAgreement;

    @Column
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRoomList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatUser> chatUserList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> postList = new ArrayList<>();
}
