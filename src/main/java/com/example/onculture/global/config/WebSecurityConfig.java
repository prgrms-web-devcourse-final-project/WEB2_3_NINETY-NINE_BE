package com.example.onculture.global.config;

import com.example.onculture.domain.user.service.UserService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

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

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성 ( 버전2 - Restful API용 )
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // 인증, 인가 설정
                // 특정 경로에 대한 액세스 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/static/**", "/styles/**", "/images/**", "/css/**", "/scripts/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/signup").permitAll()
                        // 다른 요청들은 거부
                        .anyRequest().authenticated())

                // jwt 기반 로그인/로그아웃 사용 시 사용
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용 시, 세션을 사용하지 않음
//                .httpBasic(withDefaults());  // Basic Auth 또는 JWT 인증 방식 사용

                // csrf 설정 비활성화 ( 개발 중에는 비활성화 )
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

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

    // 인증 관리자 권한 설정
    // 사용자 정보를 가져올 서비스 재정의, 인증 방법(LDAP, JDBC 기반 인증) 등 설정
    // 사용자 인증(Authentication)을 처리하는 AuthenticationManager 빈을 설정하는 코드
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, UserService userService)
            throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();       // 기본 인증 제공자( UserDetailsService를 이용해 사용자 정보를 가져오고, 비밀번호 검증을 수행)
        authProvider.setUserDetailsService(userService);    // 사용자 정보 로딩 서비스 설정 ( 반드시 UserDetailsService를 상속 받은 클래스 )
        authProvider.setPasswordEncoder(passwordEncoder);   // 비밀번호 암호화 방식 설정
        return new ProviderManager(authProvider);
    }

    // 패스워드 인코더로 사용할 빈 등록
    // pring Security 5 이상에서 기본적으로 제공하는 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // BCrypt 알고리즘만 사용하는 비밀번호 인코더 ( 사용 x )
    /*
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
     */
}
