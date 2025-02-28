package com.example.onculture.global.config;

import com.example.onculture.domain.oauth.handler.OAuth2SuccessHandler;
import com.example.onculture.domain.oauth.service.OAuth2UserCustomService;
import com.example.onculture.global.utils.jwt.JwtAuthenticationFilter;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import com.example.onculture.domain.oauth.repository.OAuth2AuthorizationRequestOnCookieRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final OAuth2AuthorizationRequestOnCookieRepository oAuth2AuthorizationRequestOnCookieRepository;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성 ( 버전2 - Restful API용 )
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 인증, 인가 설정
                // csrf, 세션 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 특정 경로에 대한 액세스 설정
                .authorizeHttpRequests(auth -> auth

                                // 인증 토큰 여부에 따라 접근 제한 (모든 권한)
//                        .requestMatchers("경로").authenticated()
                                // 권한에 따라 접근 제한
//                        .requestMatchers("경로").hasRole("ADMIN")

                                .requestMatchers("/").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/login.html").permitAll()
                                .requestMatchers("/index.html").permitAll()
                                // 정적 리소스
                                .requestMatchers(
                                        "/",
                                        "/imgs/**",
                                        "/static/index.html",
                                        "/templates/**",
                                        "/assets/**",
                                        "/css/**",
                                        "/js/**",
                                        "/favicon.ico"
                                ).permitAll()
                                // Swagger UI
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/api-docs/**"
                                ).permitAll()
                                // 다른 요청들은 거부
                                .anyRequest().authenticated()
                );

        // JWT 기반 인증 추가
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        // OAuth 로그인
        http.oauth2Login(oauth2 -> oauth2
                // OAuth2의 인가 요청을 저장하는 저장소 ??
                .authorizationEndpoint(authorizationEndpoint ->
                        // OAuth2 로그인 요청 정보를 쿠키에 저장하는 커스텀 저장소 사용 (JWT 기반 인증 사용 시, 쿠키를 많이씀)
                        authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestOnCookieRepository))
                // userInfoEndpoint() : OAuth2 로그인 후 사용자 정보를 가져오는 엔드포인트 설정
                // userService() : 사용자 정보를 처리하는 서비스 지정 ( 사용자 정보 DB 저장 및 JWT 토큰 생성 )
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService))
                // OAuth2 로그인 성공 시, 실행할 핸들러 ( JWT 토큰 발급 및 쿠키 저장, 특정 페이지로 리다이렉트 )
                .successHandler(oAuth2SuccessHandler));
        // OAuth2 로그인 실패 시, 실행할 핸들러 ( 에러 메세지 포함 및 특정 페이지 이동 )
//                .failureHandler(oAuth2FailureHandler()));

        //에러처리
        http.exceptionHandling(exception -> exception
                // 인증 실패 시 401 반환
                .authenticationEntryPoint((request, response, authException) -> {
                    // 커스텀 예외 실행
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"status\":401, \"error\":\"Unauthorized\", \"message\":\"인증이 필요합니다.\"}");
                })
                // 권한 부족 시 403 반환
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"status\":403, \"error\":\"Forbidden\", \"message\":\"권한이 부족합니다.\"}");
                })
        );

        return http.build();
    }

    // 인증 관리자 권한 설정 ( 버전 1 )
    // 사용자 정보를 가져올 서비스 재정의, 인증 방법(LDAP, JDBC 기반 인증) 등 설정
    // 사용자 인증(Authentication)을 처리하는 AuthenticationManager 빈을 설정하는 코드
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService ) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();       // 기본 인증 제공자( UserDetailsService를 이용해 사용자 정보를 가져오고, 비밀번호 검증을 수행)
        authProvider.setUserDetailsService(userDetailsService);    // 사용자 정보 로딩 서비스 설정 ( 반드시 UserDetailsService를 상속 받은 클래스 )
        authProvider.setPasswordEncoder(passwordEncoder);   // 비밀번호 암호화 방식 설정

        // 기본적으로 사용자 존재 여부를 알리지 않는다.
        // 사용자 미존재 에러(UsernameNotFoundException) -> 비밀번호 미일치 에러(BadCredentialsException)로 변환해서 반환
        // UsernameNotFoundException(사용자 존재 여부)을 숨기지 않도록 설정 ( 우선 미사용 )
//        authProvider.setHideUserNotFoundExceptions(false);

        return new ProviderManager(authProvider);
    }

    // 패스워드 인코더로 사용할 빈 등록
    // Spring Security 5 이상에서 기본적으로 제공하는 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
