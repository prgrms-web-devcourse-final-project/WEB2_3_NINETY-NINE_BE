package com.example.onculture.domain.user.controller;

import com.example.onculture.domain.event.dto.BookmarkEventListDTO;
import com.example.onculture.domain.event.service.BookmarkService;
import com.example.onculture.domain.socialPost.dto.UserPostListResponseDTO;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.LikedSocialPostIdsResponseDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "유저 API", description = "사용자 로그인 및 정보 관리")
public class UserController {

    private final UserService userService;
    private final BookmarkService bookmarkService;

    // 회원가입 API
    @Operation( summary = "회원가입 API", description = "로컬 회원가입 API" )
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<String>> signup(@RequestBody SignupRequestDTO request) {
        userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(HttpStatus.CREATED, "회원가입에 성공하였습니다."));
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
    @Operation( summary = "닉네임 중복 확인", description = "해당 닉네임으로 된 사용자가 존재하는지 확인" )
    @PostMapping("check-nickname")
    public ResponseEntity<SuccessResponse<Boolean>> nicknameOverlap(@RequestParam String nickname) {
        boolean isAlreadyNickname = userService.checkNickname(nickname);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, isAlreadyNickname));
    }

    // 다른 사용자 프로필 정보 API
    @Operation( summary = "다른 사용자 프로필 조회", description = "다른 사용자의 프로필 정보를 조회" )
    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> otherUser(@PathVariable Long userId, HttpServletRequest request) {
        UserProfileResponse userProfileResponse = userService.getUserProfile(userId);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 조회 성공", userProfileResponse));
    }

    // 로그인한 사용자 프로필 정보 API
    @Operation( summary = "현재 사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회" )
    @GetMapping("/profile")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> user(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UserProfileResponse userProfileResponse = userService.getUserProfile(customUserDetails.getUserId());
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 조회 성공", userProfileResponse));
    }

    // 로그인한 사용자 정보 수정 API
    @Operation( summary = "현재 사용자 프로필 수정", description = "현재 로그인한 유저의 정보를 수정하는 API" )
    @PutMapping( "/profile" )
    public ResponseEntity<SuccessResponse<String>> updateUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @ModelAttribute ModifyRequestDTO dto,
            @RequestPart(name = "image_data", required = false) MultipartFile imageData) {
        userService.modifyUserProfile(customUserDetails.getUserId(), dto, imageData);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, "프로필 수정 성공"));
    }

    @Operation(summary = "유저가 좋아요를 누른 SocialPost 목록 조회",
            description = "")
    @GetMapping("/users/{userId}/liked-social-posts")
    public ResponseEntity<SuccessResponse<LikedSocialPostIdsResponseDto>> getLikedSocialPosts(@PathVariable Long userId) {
        LikedSocialPostIdsResponseDto likedPosts = userService.getLikedSocialPosts(userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, likedPosts));
    }

    @Operation(summary = "유저의 게시판 전체 조회",
            description = "userId에 해당하는 게시글을 불러옵니다. pageNum과 pageSize의 기본값은 각각 0, 9입니다.")
    @GetMapping("/users/{userId}/socialPosts")
    public ResponseEntity<SuccessResponse<UserPostListResponseDTO>> getSocialPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "9") int pageSize) {
        UserPostListResponseDTO responseDTO = userService.getSocialPostsByUser(userId, pageNum, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    @Operation(summary = "유저가 북마크를 누른 공연 게시글 조회",
            description = "로그인 필수 API 입니다.")
    @GetMapping("/bookmarks/my-events")
    public ResponseEntity<SuccessResponse<BookmarkEventListDTO>> getMyBookmarkedEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);
        BookmarkEventListDTO responseDTO = userService.getBookmarkedEvents(userDetails.getUserId(), pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }
}
