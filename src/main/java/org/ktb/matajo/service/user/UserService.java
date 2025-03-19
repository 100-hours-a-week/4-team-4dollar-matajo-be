package org.ktb.matajo.service.user;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional
    public boolean updateNickname(String newNickname) {
        // 닉네임 중복 검사
        if (userRepository.existsByNickname(newNickname)) {
            return false;
        }

        // 현재 사용자를 찾고 닉네임 변경 (JWT 인증이 없으므로, 임시로 첫 번째 사용자 변경)
        Optional<User> userOptional = userRepository.findTopByOrderByIdAsc();
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        user.setNickname(newNickname);
        userRepository.save(user);
        return true;
    }
}
