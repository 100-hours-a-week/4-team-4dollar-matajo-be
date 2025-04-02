package org.ktb.matajo.service.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Service
public class KakaoAuthService {

    private final String clientId;
    private final String redirectUri;
    private final String tokenUri;

    public KakaoAuthService(
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.redirect-uri}") String redirectUri,
            @Value("${kakao.token-uri}") String tokenUri) {
        this.clientId = clientId;
        this.redirectUri = "https://matajo.store/auth/kakao";
        this.tokenUri = tokenUri;
    }

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
            Map<String, Object> body = response.getBody();
            return (String) body.get("access_token");
        } catch (HttpClientErrorException e) {
            throw e;
        }
    }
}
