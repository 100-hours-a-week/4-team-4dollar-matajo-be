package org.ktb.matajo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.ktb.matajo.entity.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
//name = 인덱스 이름, columnList = 실제데이터베이스 컬러이름
@Table(indexes = {
        @Index(name = "idx_post_address_id", columnList = "address_id"),
        @Index(name = "idx_post_keeper_id", columnList = "keeper_id")
})
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // keeper_id를 필드로 직접 사용x 관계 매핑 사용
    // 지연로딩 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keeper_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private User user;

    // 한줄 소개
    @Column(nullable = false, length = 50)
    private String title;

    // 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 희망가격
    @Column(nullable = false)
    private int preferPrice;

    // 공개 여부
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean hiddenStatus;

    // 소수점 값 제한
    @Column(nullable = false)
    private float discountRate;

    // 주소 ID
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Address address;

    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTagList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRoomList = new ArrayList<>();

    // 게시글 정보 선택적 업데이트 - title이 null이 아닐 경우에만 업데이트
    public void updateTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    // 게시글 내용 선택적 업데이트
    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    // 선호 가격 선택적 업데이트
    public void updatePreferPrice(Integer preferPrice) {
        if (preferPrice != null && preferPrice > 0) {
            this.preferPrice = preferPrice;
        }
    }

    // 할인율 선택적 업데이트
    public void updateDiscountRate(Float discountRate) {
        if (discountRate != null) {
            this.discountRate = discountRate;
        }
    }

    // 숨김 상태 선택적 업데이트
    public void updateHiddenStatus(Boolean hiddenStatus) {
        if (hiddenStatus != null) {
            this.hiddenStatus = hiddenStatus;
        }
    }

    public void updateAddress(Address address){
        this.address=address;
    }

    public void updateTag(Address address){
        this.address=address;
    }

    // 게시글 삭제 표시 (소프트 삭제)
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 게시글이 삭제되었는지 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 공개 비공개 상태 변경
    public void toggleHiddenStatus() {
        this.hiddenStatus = !this.hiddenStatus;
    }

}
