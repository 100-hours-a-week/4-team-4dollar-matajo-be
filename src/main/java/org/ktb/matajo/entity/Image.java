package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(indexes = {@Index(name = "idx_image_post_id", columnList = "post_id")})
public class Image {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id; // 이미지 ID (PK)

  @Column(nullable = false)
  private String imageUrl; // S3 파일 경로

  @Column(nullable = false, columnDefinition = "TINYINT(1)")
  private boolean thumbnailStatus; // 썸네일 여부 (0: X, 1: O)

  // 게시글과 연관된 이미지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "post_id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
      nullable = false)
  private Post post; // 게시글 (Post 테이블 참조)
}
