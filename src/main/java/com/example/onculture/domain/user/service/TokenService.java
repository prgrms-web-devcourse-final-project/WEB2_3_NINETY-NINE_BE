package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.RefreshTokenRepository;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

//    public String createNewAccessToken(String refreshToken) {
//        // 토큰 유효성 검사에 실패하면 예외 발생
//        if(!jwtTokenProvider.validateToken(refreshToken)) {
//            throw new IllegalArgumentException("Unauthorized token");
//        }
//
//        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getId();
//        User user = userService.findById(userId);
//
//        return jwtTokenProvider.
//    }
}
