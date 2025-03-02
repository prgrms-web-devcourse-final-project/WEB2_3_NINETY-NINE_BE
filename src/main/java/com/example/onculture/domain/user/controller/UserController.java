package com.example.onculture.domain.user.controller;

import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "유저 API", description = "사용자 로그인 및 정보 관리")
public class UserController {


    // 회원가입 Mock API
    @Operation( summary = "회원가입 API", description = "로컬 회원가입 API" )
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<String>> signup(@RequestBody SignupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(HttpStatus.CREATED, "회원가입에 성공하였습니다."));
    }

    // 로그인 Mock API
    @Operation( summary = "로컬 로그인 API", description = "테스트를 위해 refreshToken도 같이 반환합니다." )
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<?>> localLogin(@RequestBody LoginRequestDTO dto, HttpServletRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = new TokenResponse("액세스 토큰", "리프레시 토큰");
        return ResponseEntity.ok(SuccessResponse.success("로그인에 성공하였습니다.", tokenResponse));
    }

    // 로그아웃 Mock API
    @Operation( summary = "로그아웃 API", description = "로그아웃을 통해 token 삭제" )
    @GetMapping( "/logout" )
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "로그아웃 성공"));
    }

    // 토큰 재발급 Mock API
    @Operation( summary = "액세스 토큰 재발급 API", description = "refreshToken 토큰이 만료되어 있다면 기존 저장된 토큰 삭제 및 로그인 페이지로 리다이렉트" )
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "액세스 토큰 재발급 성공"));
    }

    // 닉네임 중복 체크 Mock API
    @Operation( summary = "닉네임 중복 확인", description = "해당 닉네임으로 된 사용자가 존재하는지 확인" )
    @PostMapping("check-nickname")
    public ResponseEntity<SuccessResponse<Boolean>> nicknameOverlap(@RequestParam String nickname) {
        boolean isAlreadyNickname = false;
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, isAlreadyNickname));
    }

    // 다른 사용자 프로필 정보 Mock API
    @Operation( summary = "다른 사용자 프로필 조회", description = "다른 사용자의 프로필 정보를 조회" )
    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> otherUser(@PathVariable Long userId, HttpServletRequest request) {
        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .nickname("현재 사용자x 다른 사용자 닉네임")
                .loginType(LoginType.LOCAL_ONLY)
                .description("현재 사용자 x 다른 사용자 한 줄 소개")
                .interests(new HashSet<>(Set.of("전시회", "팝업 스토어", "뮤지컬")))
                .profileImage("12345.jpg")
                .s3Bucket("https://your-bucket.s3.amazonaws.com/profile/12345.jpg")
                .build();
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 조회 성공", userProfileResponse));
    }

    // 로그인한 사용자 프로필 정보 Mock API
    @Operation( summary = "현재 사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회" )
    @GetMapping("/profile")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> user() {
        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .nickname("현재 사용자 닉네임")
                .loginType(LoginType.LOCAL_ONLY)
                .description("현재 사용자 한 줄 소개")
                .interests(new HashSet<>(Set.of("전시회", "팝업 스토어", "뮤지컬")))
                .profileImage("12345.jpg")
                .s3Bucket("https://your-bucket.s3.amazonaws.com/profile/12345.jpg")
                .build();
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 조회 성공", userProfileResponse));
    }

    // 로그인한 사용자 정보 수정 API
    @Operation( summary = "현재 사용자 프로필 수정", description = "현재 로그인한 유저의 정보를 수정하는 API" )
    @PutMapping( "/profile" )
    public ResponseEntity<SuccessResponse<String>> updateUser(
            @ModelAttribute ModifyRequestDTO dto,
            @RequestPart(name = "image_data", required = false) MultipartFile imageData) {
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 수정 성공"));
    }
}
