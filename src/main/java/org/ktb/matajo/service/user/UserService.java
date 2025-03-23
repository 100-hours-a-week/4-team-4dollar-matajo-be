package org.ktb.matajo.service.user;

import org.ktb.matajo.dto.user.KakaoUserInfo;

import java.util.Map;

public interface UserService {
    Map<String, String> processKakaoUser(KakaoUserInfo userInfo);

    // 닉네임 중복 확인
    boolean isNicknameAvailable(String nickname);

    // 닉네임 업데이트
    boolean updateNickname(String newNickname);
}
