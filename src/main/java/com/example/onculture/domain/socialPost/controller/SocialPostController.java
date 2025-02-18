package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SocialPostController {
    private SocialPostService socialPostService;

    @Operation(summary = "소셜 게시판 전체 조회", description = "sort 종류는 popular, latest, comments가 있고 기본값은 latest입니다")
    @GetMapping("/socialPosts")
    public ResponseEntity<PostListResponseDTO> getSocialPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "9") int pageSize) {
        PostListResponseDTO responseDTO = socialPostService.getSocialPosts(sort, pageNum, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @Operation(summary = "소셜 게시판 상세 조회", description = "socialPostId에 해당하는 게시글의 상세 조회 API 입니다")
    @GetMapping("/socialPosts/{socialPostId}")
    public ResponseEntity<PostResponseDTO> getSocialPost(@PathVariable Long socialPostId) {

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Operation(summary = "유저의 게시판 전체 조회", description = "userId에 해당하는 게시글을 불러옵니다")
    @GetMapping("/users/{userId}/socialPosts")
    public ResponseEntity<UserPostListResponseDTO> getSocialPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "1") int pageSize) {
        PostResponseDTO post1 = new PostResponseDTO();
        post1.setId(1L);
        post1.setContent("내용1");
        post1.setTitle("제목1");
        post1.setImageUrl("이미지1");
        post1.setCommentCount(3);
        post1.setLikeCount(3);
        post1.setViewCount(3);
        post1.setUserId(userId);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
        post1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        PostResponseDTO post2 = new PostResponseDTO();
        post2.setId(2L);
        post2.setContent("내용2");
        post2.setTitle("제목2");
        post2.setImageUrl("이미지2");
        post2.setCommentCount(2);
        post2.setLikeCount(2);
        post2.setViewCount(2);
        post2.setUserId(userId);
        post2.setCreatedAt(LocalDateTime.now().minusDays(2));
        post2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        PostResponseDTO post3 = new PostResponseDTO();
        post3.setId(3L);
        post3.setContent("내용3");
        post3.setTitle("제목3");
        post3.setImageUrl("이미지3");
        post3.setCommentCount(1);
        post3.setLikeCount(1);
        post3.setViewCount(1);
        post3.setUserId(userId);
        post3.setCreatedAt(LocalDateTime.now().minusDays(3));
        post3.setUpdatedAt(LocalDateTime.now().minusDays(3));

        List<PostResponseDTO> posts = new ArrayList<>();
        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        UserPostListResponseDTO responseDTO = new UserPostListResponseDTO();
        responseDTO.setPosts(posts);
        responseDTO.setPageNum(pageNum);
        responseDTO.setPageSize(pageSize);
        responseDTO.setTotalPages(100);
        responseDTO.setTotalElements(1000L);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Operation(summary = "소셜 게시판 생성", description = "소셜 게시판 생성 API 입니다.")
    @PostMapping("/socialPosts")
    public ResponseEntity<PostResponseDTO> createSocialPost(@RequestBody CreatePostRequestDTO requestDTO) {
        PostResponseDTO post1 = new PostResponseDTO();
        post1.setId(1L);
        post1.setContent(requestDTO.getContent());
        post1.setTitle(requestDTO.getTitle());
        post1.setImageUrl(requestDTO.getImageUrl());
        post1.setCommentCount(3);
        post1.setLikeCount(3);
        post1.setViewCount(3);
        post1.setUserId(1L);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
        post1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        return ResponseEntity.status(HttpStatus.CREATED).body(post1);
    }

    @Operation(summary = "소셜 게시판 수정", description = "socialPostId에 해당하는 게시글의 수정 API 입니다")
    @PutMapping("/socialPosts/{socialPostId}")
    public ResponseEntity<PostResponseDTO> updateSocialPost(@RequestBody UpdatePostRequestDTO requestDTO, @PathVariable Long socialPostId) {
        PostResponseDTO post1 = new PostResponseDTO();
        post1.setId(socialPostId);
        post1.setContent(requestDTO.getContent());
        post1.setTitle(requestDTO.getTitle());
        post1.setImageUrl(requestDTO.getImageUrl());
        post1.setCommentCount(3);
        post1.setLikeCount(3);
        post1.setViewCount(3);
        post1.setUserId(1L);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
        post1.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.OK).body(post1);
    }

    @Operation(summary = "소셜 게시판 삭제", description = "socialPostId에 해당하는 게시글의 삭제 API 입니다")
    @DeleteMapping("/socialPosts/{socialPostId}")
    public ResponseEntity<String> deleteSocialPost(@PathVariable Long socialPostId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }

    @Operation(summary = "소셜 게시판 좋아요 추가", description = "socialPostId에 해당하는 게시글의 좋아요 추가 API 입니다")
    @PostMapping("/socialPosts/{socialPostId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeBySocialPost(@PathVariable Long socialPostId) {
        LikeResponseDTO like = new LikeResponseDTO();
        like.setId(1L);
        like.setSocialPostId(socialPostId);
        like.setUserId(1L);
        like.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(like);
    }

    @Operation(summary = "소셜 게시판 좋아요 추가", description = "socialPostId에 해당하는 게시글의 좋아요 삭제 API 입니다")
    @DeleteMapping("/socialPosts/{socialPostId}/likes/{likeId}")
    public ResponseEntity<String> deleteLikeBySocialPost(@PathVariable Long socialPostId, @PathVariable Long likeId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }
}
