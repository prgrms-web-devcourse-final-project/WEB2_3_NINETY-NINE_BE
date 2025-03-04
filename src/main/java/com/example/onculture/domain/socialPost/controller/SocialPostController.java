package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.SocialPostLikeService;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/socialPosts")
@AllArgsConstructor
@Tag(name = "소셜 게시판 API", description = "소셜 게시판을 관리하는 API")
public class SocialPostController {
    private final SocialPostService socialPostService;
    private final SocialPostLikeService socialPostLikeService;

    @Operation(summary = "소셜 게시판 전체 조회",
            description = "sort 종류는 popular, latest, comments가 있고 기본값은 latest입니다. pageNum과 pageSize의 기본값은 각각 0, 9입니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<PostListResponseDTO>> getSocialPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        PostListResponseDTO responseDTO = socialPostService.getSocialPosts(sort, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    @Operation(summary = "소셜 게시판 상세 조회", description = "socialPostId에 해당하는 게시글의 상세 조회 API 입니다")
    @GetMapping("/{socialPostId}")
    public ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> getSocialPost(
            @PathVariable Long socialPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        PostWithLikeResponseDTO responseDTO = socialPostService.getSocialPostWithLikeStatus(socialPostId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }


    @Operation(summary = "소셜 게시판 생성", description = "소셜 게시판 생성 API 입니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> createSocialPost(
            @RequestBody CreatePostRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostWithLikeResponseDTO responseDTO = socialPostService.createSocialPost(
                userDetails.getUserId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success(HttpStatus.CREATED, responseDTO));
    }

    @Operation(summary = "소셜 게시판 수정", description = "socialPostId에 해당하는 게시글의 수정 API 입니다")
    @PutMapping("/{socialPostId}")
    public ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> updateSocialPost(
            @RequestBody UpdatePostRequestDTO requestDTO,
            @PathVariable Long socialPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostWithLikeResponseDTO responseDTO = socialPostService.updateSocialPost(
                userDetails.getUserId(), requestDTO, socialPostId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    @Operation(summary = "소셜 게시판 삭제", description = "socialPostId에 해당하는 게시글의 삭제 API 입니다")
    @DeleteMapping("/{socialPostId}")
    public ResponseEntity<SuccessResponse<String>> deleteSocialPost(
            @PathVariable Long socialPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String result = socialPostService.deleteSocialPost(
                userDetails.getUserId(), socialPostId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, result));
    }

    @Operation(summary = "소셜 게시판 좋아요 토글",
            description = "좋아요를 누른 유저가 요청하면 삭제, 누르지 않은 유저가 요청하면 추가됩니다.")
    @PostMapping("/{socialPostId}/likes")
    public ResponseEntity<SuccessResponse<String>> toggleLike(
            @PathVariable Long socialPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String result = socialPostLikeService.toggleLike(
                userDetails.getUserId(), socialPostId);

        if (result.equals("좋아요 추가")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success(HttpStatus.CREATED, result));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, result));
        }
    }
}

