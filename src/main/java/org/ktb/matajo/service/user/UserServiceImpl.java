package org.ktb.matajo.service.user;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public Map<String, String> processKakaoUser(KakaoUserInfo userInfo) {
    // 새로운 User 객체 생성
    User user = new User();
    user.setKakaoId(userInfo.getKakaoId().toString());  // Long을 String으로 변환해서 저장
    user.setEmail(userInfo.getEmail()); // 이메일 저장
    user.setNickname(userInfo.getNickname());
    user.setPhoneNumber(userInfo.getPhoneNumber());
    user.setRole(userInfo.getRole() != null ? userInfo.getRole() : "USER"); // role을 하드코딩하거나 전달된 값 사용

    // DB에 유저 정보 저장
    userRepository.save(user);

    // 로그인 후 반환될 JWT 토큰과 리프레시 토큰을 더미로 반환 (예시)
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", "dummyAccessToken");
    tokens.put("refreshToken", "dummyRefreshToken");
    return tokens;
  }

  @Override
  public boolean isNicknameAvailable(String nickname) {
    // 닉네임 중복 확인 로직 (예시)
    User user = userRepository.findByNickname(nickname);
    return user == null; // 중복이 없다면 true
  }

  @Override
  public boolean updateNickname(String newNickname) {
    // 닉네임 업데이트 로직 (예시)
    // 특정 사용자를 찾아 닉네임을 업데이트하는 로직 추가
    User user = userRepository.findById(1L).orElse(null); // 예시로 1번 id 사용자
    if (user != null) {
      user.setNickname(newNickname);
      userRepository.save(user);
      return true;
    }
    return false;
  }
}
