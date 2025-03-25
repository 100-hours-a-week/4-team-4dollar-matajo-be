package org.ktb.matajo.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ktb.matajo.entity.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

        if (claims != null) {
            Long userId = ((Number) claims.get("userId")).longValue();
            String nickname = (String) claims.get("nickname");
            String role = (String) claims.get("role");

            CustomUserDetails userDetails = new CustomUserDetails(userId, nickname, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    // JWT 토큰에서 사용자 ID 추출
    String token = authorizationHeader.substring(7);
    Claims claims = jwtUtil.parseToken(token);

    if (claims != null) {
      String userId = claims.getSubject();
      UserDetails userDetails = new User(userId, "", Collections.emptyList());
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    chain.doFilter(request, response);
  }
}
