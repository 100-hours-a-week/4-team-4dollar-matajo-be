package org.ktb.matajo.service.user;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.dto.user.KeeperRegisterResponseDto;

import java.util.Map;


public interface UserService {

    // 닉네임이 사용 가능한지 여부를 확인합니다.
    boolean isNicknameAvailable(String nickname);

    // 사용자의 닉네임을 새로운 값으로 업데이트합니다.
    boolean updateNickname(Long userId, String newNickname);

    // 사용자를 보관인으로 등록하고 응답 DTO를 반환합니다.
    KeeperRegisterResponseDto registerKeeper(Long userId);

    // 카카오 사용자 정보를 처리하고 JWT 토큰(access, refresh)을 반환합니다.
    Map<String, String> processKakaoUser(KakaoUserInfo userInfo);

    Map<String, String> reissueAccessToken(String refreshToken);
}