package org.ktb.matajo.service.user;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.ktb.matajo.dto.user.*;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.oauth.KakaoAuthService;
import org.springframework.transaction.annotation.Transactional;
import org.ktb.matajo.entity.RefreshToken;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.entity.UserType;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.RefreshTokenRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final KakaoAuthService kakaoAuthService;
    private final KakaoUserService kakaoUserService;

    public UserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil, KakaoAuthService kakaoAuthService, KakaoUserService kakaoUserService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.kakaoAuthService = kakaoAuthService;
        this.kakaoUserService = kakaoUserService;
    }

    @Override
    @Transactional
    public LoginResponseDto loginWithKakao(String code, HttpServletResponse response) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String kakaoAccessToken;
        try {
            kakaoAccessToken = kakaoAuthService.getAccessToken(code);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }

        KakaoUserInfo userInfo;
        try {
            userInfo = kakaoUserService.getUserInfo(kakaoAccessToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KAKAO_USERINFO_FETCH_FAILED);
        }

        TokenResponseDto tokens = processKakaoUser(userInfo);

        // 리프레시 토큰 쿠키 설정
        response.addHeader("Set-Cookie", "refreshToken=" + tokens.getRefreshToken()
                + "; HttpOnly; Path=/; Max-Age=1209600");

        return new LoginResponseDto(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                userInfo.getNickname()
        );

    }

    @Override
    @Transactional
    public TokenResponseDto processKakaoUser(KakaoUserInfo userInfo) {
        // 카카오 ID로 기존 사용자를 찾거나, 새로 등록
        Optional<User> optionalUser = userRepository.findByKakaoId(userInfo.getKakaoId());

        User user = optionalUser.orElseGet(() -> {
            // 닉네임 중복을 피하기 위해 랜덤으로 고유한 닉네임 생성
            String uniqueNickname = generateUniqueNickname();

            // 새로운 사용자 등록
            User newUser = User.builder()
                    .kakaoId(userInfo.getKakaoId())
                    .nickname(uniqueNickname)
                    .username(userInfo.getNickname())
                    .phoneNumber(userInfo.getPhoneNumber())
                    .role(UserType.USER)
                    .keeperAgreement(false)
                    .build();
            return userRepository.save(newUser);
        });

        // 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole().toString(), user.getNickname(), user.getDeletedAt());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        // 리프레시 토큰을 DB에 저장 (이전 토큰이 있으면 업데이트)
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken))
                );

        return new TokenResponseDto(accessToken, refreshToken);
    }

    // 고유한 닉네임을 생성하는 메서드
    private String generateUniqueNickname() {
        Random random = new Random();
        String nickname;
        do {
            int randomNumber = 10000 + random.nextInt(90000); // 10000 ~ 99999 범위의 랜덤 숫자 생성
            nickname = "타조" + randomNumber;
        } while (userRepository.existsByNickname(nickname)); // 중복되면 다시 생성

        return nickname;
    }

    @Override
    @Transactional
    public TokenResponseDto reissueAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = Long.valueOf(claims.getSubject());

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user.getId(),
                user.getRole().toString(),
                user.getNickname(),
                user.getDeletedAt());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

        savedToken.updateToken(newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout() {
        Long userId;
        try {
            userId = SecurityUtil.getCurrentUserId();
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.REQUIRED_AUTHORIZATION);
        }

        RefreshToken token = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        refreshTokenRepository.delete(token);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public AccessTokenResponseDto updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (newNickname == null || newNickname.trim().length() < 2 || newNickname.trim().length() > 10) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // 필요시 ErrorCode 새로 정의해도 됨
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.changeNickname(newNickname);
        userRepository.save(user);

        // 수정된 닉네임과 role을 반영한 새 AccessToken 생성
        String newAccessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getRole().toString(),
                user.getNickname(),
                user.getDeletedAt()
        );

        // AccessTokenResponseDto에 담아서 반환
        return new AccessTokenResponseDto(newAccessToken);
    }

    @Override
    @Transactional
    public AccessTokenResponseDto registerKeeper(KeeperRegisterRequestDto request ,Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == UserType.KEEPER) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        if (request.getPrivacyPolicy() == false || request.getTermsOfService() == false) {
            throw new BusinessException(ErrorCode.REQUIRED_AGREEMENT_MISSING);
        }

        user.promoteToKeeper(); // 보관인으로 승격

        // KEEPER 역할로 변경된 accessToken 발급
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole().toString(), user.getNickname(), user.getDeletedAt());

        return new AccessTokenResponseDto(accessToken);
    }
}
