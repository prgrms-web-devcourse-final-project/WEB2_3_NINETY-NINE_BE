package com.example.onculture.domain.oauth.handler;

import com.example.onculture.domain.oauth.dto.CustomOAuth2User;
import com.example.onculture.domain.oauth.repository.OAuth2AuthorizationRequestOnCookieRepository;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.domain.user.service.TokenService;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizationRequestOnCookieRepository authorizationRequestBasedOnCookieRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oauth2User.getEmail();
        System.out.println("email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));

        Long userId = user.getId();
        Role role = user.getRole();

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, email, role);
        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = tokenService.createRefreshToken(userId);

        // 액세스 토큰 및 리프레시 토큰을 쿠키에 저장
        tokenService.addAllTokenToCookie(request, response, accessToken, refreshToken);
        // 인증 관련 설정값, 쿠키 제거
        clearAuthenticationAttributes(request, response);
        // 헤더에 엑세스 토큰 추가
        response.setHeader("Authorization", "Bearer " + accessToken);

        // application.properties 에서 리다이렉트 uri 변경 가능 ( 기본값 : 백엔드 테스트용 리다이렉트 uri )
        getRedirectStrategy().sendRedirect(request, response, redirectUri+"?access_token=Bearer "+accessToken);
    }

    // 인증 관련 설정값, 쿠키 제거
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
    }
}
