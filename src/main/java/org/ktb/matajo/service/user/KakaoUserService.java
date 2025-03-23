package org.ktb.matajo.service.user;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.springframework.stereotype.Service;

@Service
public class KakaoUserService {

  public KakaoUserInfo getUserInfo(String accessToken) {
    // 더미 데이터를 반환 (실제 카카오 API 호출 부분은 더미로 처리)
    return new KakaoUserInfo(
            9999L,                // 더미 카카오 ID
            "dummy@example.com",  // 더미 이메일
            "더미유저",            // 더미 닉네임
            "010-1234-5678",      // 더미 전화번호
            "USER"                // 더미 역할
    );
  }
}
