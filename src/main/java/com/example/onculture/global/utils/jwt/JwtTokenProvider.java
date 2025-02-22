package com.example.onculture.global.utils.jwt;

import com.example.onculture.domain.user.domain.Interest;
import com.example.onculture.domain.user.domain.Role;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;

@Slf4j
@Getter
@Setter
@Component
public class JwtTokenProvider {

    private final Key key;      // 서명 키
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // application.properties에 설정된 값 반영
    public JwtTokenProvider(
            // @Value의 임포트 경로 : org.springframework.beans.factory.annotation.Value
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성 (버전 2)
    public String createAccessToken(Authentication authentication) {

        // Authentication 객체에서 UserDetails 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        Role role = userDetails.getRole();

        return createToken(userId, email, role, accessTokenExpiration);
    }

    // Refresh Token 생성 (버전 2)
    public String createRefreshToken(Authentication authentication) {

        // Authentication 객체에서 UserDetails 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        Role role = userDetails.getRole();

        return createToken(userId, email, role, refreshTokenExpiration);
    }

    // JWT 토큰 생성 메서드 (버전 2) - 기존 인증 객체 -> 개별 데이터로 변환 (다형성)
    // 권한 관리를 위해 무조건 Role 타입으로 강제
    private String createToken(Long userId, String email, Role role , long expiry) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiry);

        // JWT 생성
        JwtBuilder builder = Jwts.builder()
                .setId(userId.toString())
                .setSubject(email)
                .setIssuedAt(now)
                .claim("role", role.name())
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256);

        return builder.compact();
    }

    // Refresh Token으로 새로운 Access Token 생성하기
    public String reGenerateToken(String refreshToken) {

        Claims claims = getClaims(refreshToken);
        Long userId = Long.parseLong(claims.getId());
        String email = claims.getSubject();
        // role을 String으로 먼저 가져오고, Role Enum으로 변환 ( 이것 때문에 에러나서 헤맸다. )
        String roleString = claims.get("role", String.class);
        Role role = Role.valueOf(roleString);

        return createToken(userId, email, role, accessTokenExpiration);
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
            System.out.println("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            System.out.println("토큰이 유효하지 않습니다.");
        }
        return false;
    }

    // 토큰에서 유저 ID 추출 ( 타입 : Long )
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getId());
    }

    // 토큰에서 유저 email 추출
    public String getUserEmailFromToken (String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 유저 role(권한) 추출
    public String getRoleFromToken (String token) {
        return getClaims(token).get("role").toString();
    }

    // 요청(request) header에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    // 토큰 기반으로 인증 정보 가져오기
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        // JWT에서 가져온 role(권한) 정보를 GrantedAuthority로 변환
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role").toString()));
        // UsernamePasswordAuthenticationToken을 생성하여 Spring Security 인증 객체로 반환
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    }

    // 토큰에서 Claims(토큰 내용)를 추출하는 메서드
    private Claims getClaims(String token) {

        try {
            return Jwts.parser()    // JWT 파싱
                    .setSigningKey(key)     // 서명 키 설정
                    .build()
                    .parseClaimsJws(token)      // JWT를 Claims 객체로 파싱
                    .getBody();     // JWT의 본문인 Claims 객체 반환

        } catch (ExpiredJwtException e) {
            log.info("토큰에서 Claims(토큰 내용)를 추출 실패 ");
            throw new CustomException.CustomInvalidTokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    // JWT 토큰 생성 메서드 (버전 1)
    /*
    private String createToken(Authentication authentication , long expiry) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiry);

        // Authentication 객체에서 UserDetails 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 변환된 UserDetails의 각 데이터 조회
        System.out.println("User Id: " + userDetails.getUserId());
        System.out.println("User Email: " + userDetails.getEmail());
        System.out.println("User Role: " + userDetails.getRole());

        // JWT 생성
        JwtBuilder builder = Jwts.builder()
                .setId(userDetails.getUserId().toString())
                .setSubject(userDetails.getEmail())
                .setIssuedAt(now)
                .claim("role", userDetails.getRole().name())
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256);

        return builder.compact();
    }
     */

    // Access Token 생성 (버전 1)
    /*
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenExpiration);
    }
     */

    // Refresh Token 생성 (버전 1)
    /*
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenExpiration);
    }
     */
}
