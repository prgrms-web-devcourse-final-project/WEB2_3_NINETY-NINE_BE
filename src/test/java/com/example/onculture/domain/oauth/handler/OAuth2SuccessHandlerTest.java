package com.example.onculture.domain.oauth.handler;

import com.example.onculture.domain.oauth.dto.CustomOAuth2User;
import com.example.onculture.domain.oauth.repository.OAuth2AuthorizationRequestOnCookieRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.domain.user.service.TokenService;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuth2SuccessHandlerTest {
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OAuth2AuthorizationRequestOnCookieRepository authorizationRequestBasedOnCookieRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomOAuth2User oauth2User;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    // @Test
    // @DisplayName("사용자가 발견되면 리디렉션해야 합니다")
    // void onAuthenticationSuccess_shouldRedirectWhenUserFound() throws IOException, ServletException {
    //     // Given
    //     String email = "test@gmail.com";
    //     Long userId = 1L;
    //     Role role = Role.USER;
    //     String nickname = "TestUser";
    //     Set<Social> socials = new HashSet<>();
    //     socials.add(Social.LOCAL); // 예시로 LOCAL 소셜 추가
    //
    //     // CustomOAuth2User Mock 설정
    //     Map<String, Object> attributes = Map.of("email", email, "nickname", nickname);
    //     oauth2User = new CustomOAuth2User(attributes, "google", email, nickname, role);
    //     when(authentication.getPrincipal()).thenReturn(oauth2User);
    //
    //     // 사용자 Mock 설정
    //     User user = User.builder()
    //             .id(userId)
    //             .email(email)
    //             .nickname(nickname)
    //             .role(role)
    //             .loginType(LoginType.LOCAL_ONLY)  // 기본값
    //             .socials(socials)
    //             .build();
    //     when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    //
    //     // 액세스 토큰, 리프레시 토큰 Mock 설정
    //     String accessToken = "accessToken";
    //     String refreshToken = "refreshToken";
    //     when(jwtTokenProvider.createAccessToken(userId, email, role)).thenReturn(accessToken);
    //     when(tokenService.createRefreshToken(userId)).thenReturn(refreshToken);
    //
    //     // 쿠키에 토큰을 저장하는 메서드가 제대로 호출되었는지 확인
    //     doNothing().when(tokenService).addAllTokenToCookie(request, response, accessToken, refreshToken);
    //
    //     // 리다이렉트 URI 설정
    //     String redirectUri = "http://localhost:8080/";
    //     ReflectionTestUtils.setField(oAuth2SuccessHandler, "redirectUri", redirectUri);
    //
    //     // When
    //     oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
    //
    //     // Then
    //     // 응답 상태 코드가 302(리디렉션)인지 확인
    //     assertEquals(302, response.getStatus());
    //     // 리다이렉트 URL 확인
    //     assertEquals("http://localhost:8080/?access_token=accessToken", response.getRedirectedUrl());
    //
    //     // 헤더의 Authorization에 액세스 토큰이 들어있는지 검증
    //     assertEquals("Bearer " + accessToken, response.getHeader("Authorization"));
    //
    //     // userRepository.findByEmail 호출 여부 검증
    //     verify(userRepository, times(1)).findByEmail(email);
    //
    //     // 액세스 토큰과 리프레시 토큰을 쿠키에 추가하는 메서드가 호출되었는지 검증
    //     verify(tokenService, times(1)).addAllTokenToCookie(request, response, accessToken, refreshToken);
    // }

    @Test
    @DisplayName("socialUpdate 메서드에서 loginType 및 socials를 올바르게 업데이트해야 합니다.")
    void socialUpdate_shouldCorrectlyUpdateLoginTypeAndSocials() {
        // Given
        String email = "test@gmail.com";
        Long userId = 1L;
        Role role = Role.USER;
        String nickname = "TestUser";
        Set<Social> socials = new HashSet<>();
        socials.add(Social.LOCAL); // 예시로 LOCAL 소셜 추가

        // 사용자 Mock 설정
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(role)
                .loginType(LoginType.LOCAL_ONLY)  // 기본값
                .socials(socials)
                .build();

        // socialUpdate 메서드를 테스트하는 부분
        user.socialUpdate(Social.GOOGLE, new HashSet<>(Set.of(Social.GOOGLE, Social.LOCAL)));

        // Then
        assertEquals(LoginType.BOTH, user.getLoginType());  // LoginType이 BOTH로 변경되어야 함
        assertTrue(user.getSocials().contains(Social.GOOGLE));  // socials에 GOOGLE이 추가되어야 함
    }
}
