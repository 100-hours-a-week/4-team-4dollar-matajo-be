package org.ktb.matajo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config-path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        try {
            // Firebase 서비스 계정 키 파일 로드
            InputStream serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();

            // Firebase 초기화
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 초기화 오류 방지를 위해 Firebase 앱이 이미 존재하는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options, "matajo-app");
                log.info("Firebase 애플리케이션이 초기화되었습니다");
                return FirebaseMessaging.getInstance(app);
            } else {
                log.info("Firebase 애플리케이션이 이미 초기화되어 있습니다");
                return FirebaseMessaging.getInstance();
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}