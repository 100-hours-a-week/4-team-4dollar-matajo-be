package org.ktb.matajo.service.user;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import java.util.Map;

public interface UserService {
    Map<String, String> processKakaoUser(KakaoUserInfo userInfo);

    boolean isNicknameAvailable(String nickname);

    boolean updateNickname(String newNickname);
}
