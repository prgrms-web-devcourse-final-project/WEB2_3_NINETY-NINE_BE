package com.example.onculture.global.utils.jwt;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@Component
public class JwtTokenProvider {

    private final Key key;      // 서명 키
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final UserRepository userRepository;

    // application.properties에 설정된 값 반영
    public JwtTokenProvider(
            // @Value의 임포트 경로 : org.springframework.beans.factory.annotation.Value
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userRepository = getUserRepository();
    }

    // Access Token 생성
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenExpiration);
    }

    // Refresh Token 생성
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenExpiration);
    }


    // JWT 토큰 생성 메서드
    private String createToken(Authentication authentication , long expiry) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiry);

        // Authentication 객체에서 UserDetails 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        System.out.println("User Email: " + userDetails.getEmail());
        System.out.println("User Role: " + userDetails.getRole());

        // JWT 생성
        JwtBuilder builder = Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(now)
                .claim("role", userDetails.getRole())
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256);

        if ( userDetails.getRole() != null) {
            builder.claim("role", userDetails.getRole().name());        // name() : 문자열로 변환
        }

        return builder.compact();
    }

    // 토큰에서 유저 email 추출
    public String getUserEmailFromToken (String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 유저 role(권한) 추출
    public String getRoleFromToken (String token) {
        return getClaims(token).get("role").toString();
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
            // 이거는 있다가 좀 더 알아보자
            Jwts.parser().setSigningKey(key)    // 비밀값으로 복호화
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다.");
        } catch (JwtException e) {
            log.error("JWT 토큰이 유효하지 않습니다.");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(claims.get("role").toString()));

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }

    // 토큰에서 Claims를 추출하는 메서드
    private Claims getClaims(String token) {
        return Jwts.parser()    // JWT 파싱
                .setSigningKey(key)     // 서명 키 설정
                .build()
                .parseClaimsJws(token)      // JWT를 Claims 객체로 파싱
                .getBody();     // JWT의 본문인 Claims 객체 반환
    }
}
