package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.RefreshToken;
import com.example.onculture.domain.user.repository.RefreshTokenRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.utils.CookieUtil;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenService tokenService;

    private final Long TEST_USER_ID = 1L;
    private final String TEST_ACCESS_TOKEN = "testAccessToken";
    private final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshToken = RefreshToken.builder()
                .userId(TEST_USER_ID)
                .refreshToken(TEST_REFRESH_TOKEN)
                .build();
    }

    @Test
    @DisplayName("생성된 액세스 및 리프레시 토큰을 쿠키에 저장")
    void addAllTokenToCookie() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        tokenService.addAllTokenToCookie(request, response, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);

        // Then
        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains(CookieUtil.ACCESS_TOKEN_COOKIE_NAME));
        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains(TEST_ACCESS_TOKEN));
        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains(CookieUtil.REFRESH_TOKEN_COOKIE_NAME));
        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains(TEST_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 저장 - 기존 토큰이 없는 경우")
    void createRefreshToken_noExistingToken() {
        when(jwtTokenProvider.createRefreshToken(TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);
        when(refreshTokenRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String result = tokenService.createRefreshToken(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_REFRESH_TOKEN, result);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 저장 - 기존 토큰이 있는 경우 업데이트")
    void createRefreshToken_existingToken() {
        when(jwtTokenProvider.createRefreshToken(TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);
        when(refreshTokenRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String result = tokenService.createRefreshToken(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_REFRESH_TOKEN, result);
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 삭제")
    void deleteRefreshToken() {
        when(refreshTokenRepository.findByRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));

        tokenService.deleteRefreshToken(TEST_REFRESH_TOKEN);

        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰을 사용해 새로운 액세스 토큰 생성")
    void createAccessTokenFromRefreshToken() {
        when(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_USER_ID);
        when(refreshTokenRepository.findByRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.reGenerateToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_ACCESS_TOKEN);

        String newAccessToken = tokenService.createAccessTokenFromRefreshToken(TEST_REFRESH_TOKEN);

        assertNotNull(newAccessToken);
        assertEquals(TEST_ACCESS_TOKEN, newAccessToken);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 시 예외 발생")
    void createAccessTokenFromRefreshToken_invalidToken() {
        when(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).thenReturn(false);

        assertThrows(CustomException.CustomInvalidTokenException.class,
                () -> tokenService.createAccessTokenFromRefreshToken(TEST_REFRESH_TOKEN));
    }
}
