package com.example.onculture.domain.user.controller;

import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.dto.response.UserResponse;
import com.example.onculture.domain.user.service.RefreshTokenService;
import com.example.onculture.domain.user.service.UserService;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "유저 API", description = "사용자 로그인 및 정보 관리")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMillis;

    // 성공 응답 생성
    public Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);
        response.put("data", data);

        return response;
    }

    // request 에 있는 쿠키에서 refreshToken 값 가져오기
    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    String value = cookie.getValue();
                    // 쿠키에 있던 refreshToken가 null 및 빈값일 경우 null 로 반환
                    return (value != null && !value.trim().isEmpty()) ? value : null;
                }
            }
        }
        return null;
    }

    // 회원가입 API
    @Operation( summary = "회원가입 API", description = "로컬 회원가입 API" )
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Void>> signup(@RequestBody SignupRequestDTO request) {
        User user = userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(HttpStatus.CREATED, "회원가입에 성공하였습니다.", null));
    }

    // 로그인 API
    @Operation( summary = "로컬 로그인 API", description = "테스트를 위해 refreshToken도 같이 반환합니다." )
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<?>> login(@RequestBody LoginRequestDTO dto) {

        // 사용자 인증 메서드 실행
        Authentication authentication = userService.authenticate(dto);

        // 엑세스 토큰 및 리프레시 토큰 생성
        // Authentication 객체에서 UserDetails 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        Role role = userDetails.getRole();
        String accessToken = "Bearer " + jwtTokenProvider.createAccessToken(userId, email, role);
        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = refreshTokenService.createRefreshToken(userId);

        System.out.println("accessToken: " + accessToken);
        System.out.println("refreshToken: " + refreshToken);
        System.out.println("refreshTokenExpirationMillis: " + refreshTokenExpirationMillis);

        // 액세스 토큰, 리프레시 토큰 둘 다 저장

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)     // JavaScript에서 접근 불가 (보안 강화)
                .secure(true)       // HTTPS에서만 쿠키 전송 (보안 강화)
                .path("/")      // 쿠키가 모든 경로에서 사용 가능
                .maxAge(Duration.ofMillis(refreshTokenExpirationMillis))        // 밀리초 → Duration 변환
                .sameSite("Strict")     // 다른 도메인 요청에서는 전송되지 않음 (CSRF 방지)
                .build();

        // 테스트용 반환 DTO 및 응답 형식
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, String.valueOf(refreshTokenCookie))
                .body(SuccessResponse.success("로그인에 성공하였습니다.", tokenResponse));

        // 실제 반환용 코드
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, String.valueOf(refreshTokenCookie))
//                .body(SuccessResponse.success("로그인에 성공하였습니다."));
    }

    // 로그아웃 API
    @Operation( summary = "로그아웃 API", description = "로그아웃을 통해 token 삭제" )
    @GetMapping( "/logout" )
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        // request를 통해 쿠키에서 refreshToken 가져오기
        String refreshToken = extractRefreshToken(request);

        // refreshToken 쿠키가 있을 경우, DB에서 삭제
        if ( refreshToken != null ) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        // 클라이언트 쿠키에서 RefreshToken 삭제 (Set-Cookie 헤더 사용)
        ResponseCookie deletedCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // 즉시 만료
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, String.valueOf(deletedCookie))
                .body(SuccessResponse.success(HttpStatus.OK, "로그아웃 성공", null));
    }

    // 액세스 토큰 재발급 API
    @Operation( summary = "액세스 토큰 재발급 API", description = "refreshToken 토큰이 만료되어 있다면 기존 저장된 토큰 삭제 및 로그인 페이지로 리다이렉트" )
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<String>> refreshToken(
            HttpServletRequest request ) {

        // request를 통해 쿠키에서 refreshToken 가져오기
        String refreshToken = extractRefreshToken(request);

        // 리프레시 토큰 만료로 없을 경우, 재로그인 요청 메세지 반환
        if (refreshToken == null) {
            throw new CustomException.CustomInvalidTokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 액세스 재발급 메서드 실행
        String accessToken = refreshTokenService.createAccessTokenFromRefreshToken(refreshToken);
        log.info("재발급된 액세스 토큰 : " + accessToken);

        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "액세스 토큰 재발급 성공", accessToken));
    }

    // 로그인한 사용자 정보 반환 Mock API
    @Operation( summary = "유저 전체 정보 조회 Mock API", description = "현재 로그인한 유저의 모든 정보를 반환하는 API" )
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> user(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        System.out.println("customUserDetails: " + customUserDetails);
        try {
            System.out.println(customUserDetails.getUserId());
            System.out.println(customUserDetails.getEmail());
            System.out.println(customUserDetails.getPassword());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        UserResponse userResponse = UserResponse.builder()
                .email("testuser@gmail.com")
                .nickname("Test User")
                .description("안녕하세요. 테스트 유저입니다.")
                .role(Role.USER)  // Role enum 값 설정
                .createdAt(LocalDateTime.now())
                .interests(List.of(Interest.M, Interest.D, Interest.F, Interest.C))  // Interest enum 값 설정
                .build();

        return ResponseEntity.ok(successResponse("회원정보를 가져오기에 성공했습니다.", userResponse));
    }

    // 사용자 정보 수정 Mock API
    @Operation( summary = "사용자 정보 수정 Mock API", description = "현재 로그인한 유저의 정보를 수정하는 API" )
    @PutMapping( "/user" )
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody ModifyRequestDTO dto, HttpServletRequest request ) {

        // 사용자 데이터 업데이트 로직 실행

        // 응답 반환
        return ResponseEntity.ok(successResponse("사용자 정보가 수정되었습니다.", dto));
    }
}
