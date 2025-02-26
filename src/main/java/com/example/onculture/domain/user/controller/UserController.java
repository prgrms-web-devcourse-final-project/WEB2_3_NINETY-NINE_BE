package com.example.onculture.domain.user.controller;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import com.example.onculture.domain.user.service.UserService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "유저 API", description = "사용자 로그인 및 정보 관리")
public class UserController {

    private final UserService userService;
    @Value("${jwt.refresh-token-expiration}")


    // 성공 응답 생성
    public Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);
        response.put("data", data);

        return response;
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
    public ResponseEntity<SuccessResponse<?>> localLogin(@RequestBody LoginRequestDTO dto, HttpServletRequest request, HttpServletResponse response) {
        // 테스트용으로 응답 데이터에 액세스 토큰, 리프레시 토큰 반환 ( 최종 배포 시, 모든 토큰을 쿠키에 넣어서 반환 )
        TokenResponse tokenResponse = userService.login(dto, request, response);
        return ResponseEntity.ok(SuccessResponse.success("로그인에 성공하였습니다.", tokenResponse));
    }

    // 로그아웃 API
    @Operation( summary = "로그아웃 API", description = "로그아웃을 통해 token 삭제" )
    @GetMapping( "/logout" )
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "로그아웃 성공"));
    }

    // 액세스 토큰 재발급 API
    @Operation( summary = "액세스 토큰 재발급 API", description = "refreshToken 토큰이 만료되어 있다면 기존 저장된 토큰 삭제 및 로그인 페이지로 리다이렉트" )
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        userService.refreshToken(request, response);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "액세스 토큰 재발급 성공"));
    }

    // 닉네임 중복 체크 API
    @PostMapping("check-nickname")
    public ResponseEntity<SuccessResponse<Boolean>> nicknameOverlap(@RequestParam String nickname) {
        boolean isAlreadyNickname = userService.checkNickname(nickname);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, isAlreadyNickname));
    }

    // 로그인한 사용자 정보 반환 Mock API
    @Operation( summary = "현재 사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회" )
    @GetMapping("/user")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> user(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UserProfileResponse userProfileResponse = userService.getUserProfile(customUserDetails.getEmail());
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "조회 성공", userProfileResponse));
    }

    // 사용자 정보 수정 Mock API
//    @Operation( summary = "사용자 정보 수정 Mock API", description = "현재 로그인한 유저의 정보를 수정하는 API" )
//    @PutMapping( "/user" )
//    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody ModifyRequestDTO dto, HttpServletRequest request ) {
//
//        // 사용자 데이터 업데이트 로직 실행
//
//        // 응답 반환
//        return ResponseEntity.ok(successResponse("사용자 정보가 수정되었습니다.", dto));
//    }
}
