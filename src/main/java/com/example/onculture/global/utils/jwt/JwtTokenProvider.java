package com.example.onculture.global.utils.jwt;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Getter
@Setter
@Component
public class JwtTokenProvider {

    private final Key key;      // 서명 키
    private static final Long ACCESS_TOKEN_EXPIRATION_IN_SECONDS = Duration.ofHours(1).getSeconds();
    private static final Long REFRESH_TOKEN_EXPIRATION_IN_SECONDS = Duration.ofDays(14).getSeconds();
    public final UserRepository userRepository;

    // application.properties에 설정된 값 반영
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userRepository = userRepository;
    }

    // Access Token 생성 (버전 3)
    public String createAccessToken(Long userId, String email, Role role) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_IN_SECONDS * 1000L);

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

    // Refresh Token 생성 (버전 3)
    public String createRefreshToken(Long userId) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_IN_SECONDS * 1000L);

        // JWT 생성
        JwtBuilder builder = Jwts.builder()
                .setId(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256);

        return builder.compact();
    }

    // Refresh Token으로 새로운 Access Token 생성하기
    public String reGenerateToken(String refreshToken) {

        Claims claims = getClaims(refreshToken);
        Long userId = Long.parseLong(claims.getId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));

        userId = user.getId();
        String email = user.getEmail();
        Role role = user.getRole();

        return createAccessToken(userId, email, role);
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
            // 이거는 있다가 좀 더 알아보자
            Jwts.parser()
                    .setSigningKey(key)    // 비밀값으로 복호화
                    .setAllowedClockSkewSeconds(30)
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

    // 토큰에 있는 인증정보로 인증 객체 생성 메서드 ( 요청 맵핑 메서드의 @AuthenticationPrincipal CustomUserDetails 인자로 활용됨 )
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long userId = Long.parseLong(claims.getId());
        String email = claims.getSubject();
        Role role = Role.valueOf(claims.get("role").toString());
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, "", role);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
}
