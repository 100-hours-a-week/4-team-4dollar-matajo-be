package org.ktb.matajo.service.user;

import java.util.Map;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KakaoUserService {

  private final String userInfoUri;

  public KakaoUserService(@Value("${kakao.user-info-uri}") String userInfoUri) {
    this.userInfoUri = userInfoUri;
  }

  public KakaoUserInfo getUserInfo(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<Map> response =
        restTemplate.exchange(userInfoUri, HttpMethod.GET, request, Map.class);

    Map<String, Object> attributes = response.getBody();
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

    return new KakaoUserInfo(
        Long.valueOf(attributes.get("id").toString()),
        (String) kakaoAccount.get("email"),
        (String) profile.get("nickname"),
        (String) kakaoAccount.get("phone_number"));
  }
}
