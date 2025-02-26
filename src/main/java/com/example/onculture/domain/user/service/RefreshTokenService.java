package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.RefreshToken;
import com.example.onculture.domain.user.repository.RefreshTokenRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 리프레시 토큰 DB 저장 및 반환 메서드
    @Transactional
    public String createRefreshToken(Long userId) {

        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        try {
            // RefreshToken 테이블에서 현재 사용자의 RefreshToken 조회
            Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findByUserId(userId);

            RefreshToken savedToken;
            if (userRefreshToken.isPresent()) {
                // 기존 토큰이 있다면 업데이트 (덮어쓰기)
                RefreshToken existingToken = userRefreshToken.get();
                existingToken.update(refreshToken);  // 엔티티 - 업데이트 메서드로 덮어쓰기
                savedToken = refreshTokenRepository.save(existingToken);
            } else {
                // 기존 토큰이 없으면 새로 저장
                savedToken = refreshTokenRepository.save(RefreshToken.builder()
                        .userId(userId)
                        .refreshToken(refreshToken)
                        .build());
            }

            return savedToken.getRefreshToken();

        } catch (Exception e) {
            log.error("리프레시 토큰 저장 실패 : {}", e.getMessage());
            throw new CustomException.CustomJpaSaveException(ErrorCode.REFRESH_TOKEN_SAVE_FAILED);
        }
    }

    // 리프레시 토큰 삭제 메서드
    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        try {
            refreshTokenRepository.findByRefreshToken(refreshToken)     // DB에 refreshToken 조회 ( 없어도 빈값이 나옴으로 예외 실행 x )
                    .ifPresent(refreshTokenRepository::delete);     // DB에 refreshToken가 있을 경우 삭제 ( 삭제 중 에러 생기면 예외 실행 o )
        } catch (Exception e) {
            log.error("리프레시 토큰 삭제 실패: {}", e.getMessage(), e);
            throw new CustomException.CustomJpaDeleteException(ErrorCode.REFRESH_TOKEN_DELETE_FAILED);
        }
    }

    // 엑세스 토큰 만료 시, 리프레시 토큰으로 새로운 액세스 토큰 생성
    public String createAccessTokenFromRefreshToken(String refreshToken) {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("리프레시 토큰 검증 실패");
            throw new CustomException.CustomInvalidTokenException(ErrorCode.INVALID_TOKEN);
        }

        // 쿠키 리프레시에 있는 유저 ID 가져오기
        Long cookieRefreshTokenUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        // DB에 있는 리프레시 객체 가져오기
        RefreshToken dbRefreshToken = findByRefreshToken(refreshToken);

        // 쿠키 리프레시 토큰과 DB 리프레시 토큰의 UserId 일치 검증 ( Null을 고려하려 Objects.equals 사용 )
        if (!Objects.equals(cookieRefreshTokenUserId, dbRefreshToken.getUserId())) {
            log.error("쿠키 리프레시 토큰과 DB 리프레시 토큰이 일치하지 않습니다.");
            throw new CustomException.CustomInvalidTokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return jwtTokenProvider.reGenerateToken(refreshToken);
    }

    // 리프레시 토큰으로 리프레시 객체 반환
    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                // 일치하는 리프레시 객체가 없을 경우
                .orElseThrow(() -> new CustomException.CustomInvalidTokenException(ErrorCode.REFRESH_TOKEN_FIND_FAILED));
    }
}
