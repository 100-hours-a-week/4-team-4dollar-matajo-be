package org.ktb.matajo.service.user;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // 닉네임 중복 확인
    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    // 닉네임 변경 (JWT 인증 없음 가정)
    @Override
    @Transactional
    public boolean updateNickname(String newNickname) {
        if (userRepository.existsByNickname(newNickname)) {
            return false;
        }

        Optional<User> userOptional = userRepository.findTopByOrderByIdAsc();  // 임시 인증
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        user.setNickname(newNickname);
        userRepository.save(user);
        return true;
    }

    // 카카오 인증 없이 임시 로직 (더미 userInfo 처리)
    @Override
    public Map<String, String> processKakaoUser(KakaoUserInfo userInfo) {
        Map<String, String> response = new HashMap<>();

        // 실제 카카오 ID 없이 처리하기 위해 userInfo.getId()를 더미로 가정
        Long dummyKakaoId = userInfo.getKakaoId();  // 실제론 테스트용 값

        Optional<User> userOptional = userRepository.findByKakaoId(dummyKakaoId);

        if (userOptional.isEmpty()) {
            User newUser = User.builder()
                    .kakaoId(dummyKakaoId)
                    .nickname("test_user_" + dummyKakaoId)
                    .build();
            userRepository.save(newUser);
            response.put("message", "임시 회원가입 완료");
        } else {
            response.put("message", "기존 임시 회원 로그인");
        }

        return response;
    }
}
