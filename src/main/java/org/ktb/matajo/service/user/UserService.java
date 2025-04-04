package org.ktb.matajo.service.user;

import jakarta.servlet.http.HttpServletResponse;
import org.ktb.matajo.dto.user.*;


public interface UserService {

    // 닉네임이 사용 가능한지 여부를 확인합니다.
    boolean isNicknameAvailable(String nickname);

    // 사용자의 닉네임을 새로운 값으로 업데이트합니다.
    AccessTokenResponseDto updateNickname(Long userId, String newNickname);

    // 사용자를 보관인으로 등록하고 응답 DTO를 반환합니다.
    AccessTokenResponseDto registerKeeper(KeeperRegisterRequestDto request, Long userId);

    // 카카오 사용자 정보를 처리하고 JWT 토큰(access, refresh)을 반환합니다.
    TokenResponseDto processKakaoUser(KakaoUserInfo userInfo);

    // 리프레시 토큰을 검증하고 새로운 accessToken 및 refreshToken을 발급합니다.
    TokenResponseDto reissueAccessToken(String refreshToken);

    // 로그아웃 처리: 현재 로그인 사용자 ID의 refreshToken을 삭제합니다.
    void logout();

    // 카카오 인가 코드를 이용해 로그인 처리 후, 토큰 및 사용자 정보를 반환합니다.
    LoginResponseDto loginWithKakao(String code, HttpServletResponse response);
}