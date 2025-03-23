package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String kakaoId;
  private String email;  // 이메일 필드 추가
  private String nickname;
  private String phoneNumber;

  // role을 하드코딩으로 설정 (기본값은 "USER")
  @Column(nullable = false)
  private String role = "USER";

  private Boolean keeperAgreement;
  private String createdAt;
  private String updatedAt;
  private String deletedAt;
}
