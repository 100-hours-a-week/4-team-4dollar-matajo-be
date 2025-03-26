package org.ktb.matajo.service.user;

import java.util.Map;

import org.ktb.matajo.dto.user.KakaoUserInfo;

public interface UserService {
  Map<String, String> processKakaoUser(KakaoUserInfo userInfo);
}
