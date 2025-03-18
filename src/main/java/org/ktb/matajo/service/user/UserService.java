package org.ktb.matajo.service.user;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname); // 닉네임이 없으면 true 반환
    }
}
