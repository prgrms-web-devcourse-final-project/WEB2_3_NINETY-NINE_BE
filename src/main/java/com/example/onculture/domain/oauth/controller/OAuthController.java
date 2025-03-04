package com.example.onculture.domain.oauth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

// 프론트에서 인증 코드 받을 때 사용하는 클래스
/*
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "소셜 로그인 API", description = "인가 코드, 액세스 토큰, 소셜 로그인 관리")
public class OAuthController {

    private final OAuth2Service oAuth2Service;

    @PostMapping("/auth/callback")
    public ResponseEntity<String> oauth2Callback(@RequestParam String code, @RequestParam String provider) {
        try {
            System.out.println("code: " + code);

            System.out.println("provider: " + provider);

            // OAuth2Service에서 accessToken을 받아옵니다.
            String accessToken = oAuth2Service.getAccessToken(code, provider);

            System.out.println("accessToken: " + accessToken);

            // 받은 accessToken을 이용해 사용자 정보를 로드하거나 저장합니다.
            HashMap<String, Object> userInfo = oAuth2Service.getUserInfo(accessToken, provider);

            System.out.println("userInfo : " + userInfo);
            System.out.println("userInfo : " + userInfo.get("name"));
            System.out.println("userInfo : " + userInfo.get("email"));

            // 처리 후 JWT 토큰을 반환하거나, 로그인 성공 메시지를 반환할 수 있습니다.
            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OAuth2 authentication failed");
        }
    }
}
 */
