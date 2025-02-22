package com.example.onculture.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // JWT 인증을 위해 Authorization 및 Refresh Token을 사용하도록 설정
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme access = new SecurityScheme()        // SecurityScheme 객체 생성 및 API 보안 설정
                .type(SecurityScheme.Type.APIKEY)       // API Key 방식의 인증 (JWT를 헤더에 담아서 전송)
                .in(SecurityScheme.In.HEADER)       // HTTP 요청의 Header에서 토큰을 받음
                .name("Authorization")      // 액세스 토큰을 담을 HTTP Header 이름
                .bearerFormat("JWT");       // JWT 형식의 토큰 사용

        SecurityScheme refresh = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)       // 쿠키에서 토큰을 받음
                .name("refreshToken")       // 쿠키 이름 설정 (refreshToken)
                .bearerFormat("JWT");

        // 보안 요구 사항 설정: Authorization 및 Refresh Token 모두 필요
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token")        // 필수 인증 값 설정
                .addList("Refresh Token");      // 필수 인증 값 설정

        return new OpenAPI()
                // JWT 인증 방식 (Authorization & Refresh Token) 을 API 문서에 포함
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", access)
                        .addSecuritySchemes("Refresh Token", refresh))
                // 모든 API 요청에 대해 JWT 인증이 필요하다는 것을 명시
                .addSecurityItem(securityRequirement);
    }
}