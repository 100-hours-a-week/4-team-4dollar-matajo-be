package org.ktb.matajo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.accessTokenExpiration}") long accessTokenExpiration,
                   @Value("${jwt.refreshTokenExpiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 액세스 토큰 생성
    public String createAccessToken(Long userId, String role, String nickname, LocalDateTime deletedAt) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)  // 사용자 역할 추가
                .claim("nickname", nickname)  // 닉네임 추가
                .claim("deletedAt", deletedAt != null ? deletedAt.toString() : null)  // 탈퇴 여부 포함
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // accessToken이 만료되면 여기서 로그만 남기고 null 반환
            System.out.println("[JWT] Access token expired at: " + e.getClaims().getExpiration());
            return null;
        } catch (JwtException e) {
            // 토큰 자체가 잘못된 경우
            System.out.println("[JWT] Invalid token: " + e.getMessage());
            return null;
        }
    }

}
