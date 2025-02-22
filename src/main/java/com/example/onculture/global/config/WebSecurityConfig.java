package com.example.onculture.global.config;

import com.example.onculture.domain.user.service.UserService;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.utils.jwt.JwtAuthenticationFilter;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성 ( 버전2 - Restful API용 )
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 인증, 인가 설정
                // csrf 설정 비활성화 ( 개발 중에는 비활성화 )
                .csrf(AbstractHttpConfigurer::disable)
                // JWT 사용 시, 세션을 사용하지 않음
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
                        // 정적 리소스
                        .requestMatchers(
                                "/",
                                "/imgs/**",
                                "/index.html",
                                "/static/**",
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
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       PasswordEncoder passwordEncoder,
                                                       UserDetailsService userDetailsService ) throws Exception {
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
    // pring Security 5 이상에서 기본적으로 제공하는 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 스프링 시큐리티 기능 비활성화 ( 사용 x )
    // 정적 리소스, h2-console의 url은 인증, 인가 서비스를 적용하지 않는다.
    /*
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers("/h2-console/**")      // h2-console를 사용하지 않지만 일단 설정
                .requestMatchers("/static/**", "/styles/**", "/imgs/**", "/scripts/**");
    }
     */

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성 ( 버전 1 - Thymeleaf용 )
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // 인증, 인가 설정
                // 특정 경로에 대한 액세스 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/static/**", "/styles/**", "/images/**", "/css/**", "/scripts/**").permitAll()
//                        .requestMatchers("/h2-console/**").permitAll()      // h2-console를 사용하지 않지만 일단 설정
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/signup").permitAll()
                        // 다른 요청들은 거부
                        .anyRequest().authenticated())
                // 폼 기반 로그인 설정
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")            // 로그인 페이지 경로 설정
                        .defaultSuccessUrl("/")     // 로그인 완료 시, 이동 경로
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")     // 로그아웃 완료 시, 이동 경로
                        .invalidateHttpSession(true)    // 로그아웃 이후에 세션 전체 삭제 여부 설정
                )
                // csrf 설정 비활성화 ( 개발 중에는 비활성화 )
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
     */

    // 인증 관리자 권한 설정 ( 버전 2 / 간편한 방식 )
    /*
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
     */

    // BCrypt 알고리즘만 사용하는 비밀번호 인코더 ( 사용 x )
    /*
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
     */
}
