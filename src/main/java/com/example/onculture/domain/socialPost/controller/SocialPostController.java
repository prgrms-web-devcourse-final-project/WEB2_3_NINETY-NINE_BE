package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SocialPostController {
    @Operation(summary = "소셜 게시판 전체 조회", description = "sort 종류는 popular, latest, comments가 있고 기본값은 latest입니다")
    @GetMapping("/socialPosts")
    public ResponseEntity<PostListResponseDTO> getSocialPosts(
        @RequestParam(defaultValue = "latest") String sort,
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "3") int pageSize) {

        // 게시물 생성 (예시로 12개의 게시물 생성)
        List<PostResponseDTO> allPosts = new ArrayList<>();

        // 더미 데이터 생성
        for (long i = 1; i <= 15; i++) {
            PostResponseDTO post = new PostResponseDTO();
            post.setId(i);
            post.setContent("내용" + i);
            post.setTitle("제목" + i);
            post.setImageUrl("이미지" + i);
            post.setCommentCount((int) i);
            post.setLikeCount(20 - (int) i);
            post.setViewCount((int) i);
            post.setUserId(i);
            post.setCreatedAt(LocalDateTime.now().minusDays((int) i));
            post.setUpdatedAt(LocalDateTime.now().minusDays((int) i));
            allPosts.add(post);
        }

        // 정렬
        switch (sort) {
            case "popular":
                // likeCount 기준 내림차순 정렬
                allPosts.sort(Comparator.comparingInt(PostResponseDTO::getLikeCount).reversed());
                break;
            case "comments":
                // commentCount 기준 내림차순 정렬
                allPosts.sort(Comparator.comparingInt(PostResponseDTO::getCommentCount).reversed());
                break;
            case "latest":
            default:
                // createdAt 기준 내림차순 정렬 (기본값)
                allPosts.sort(Comparator.comparing(PostResponseDTO::getCreatedAt).reversed());
                break;
        }

        // 페이징 처리
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allPosts.size());

        List<PostResponseDTO> pagedPosts = allPosts.subList(startIndex, endIndex);

        // 응답 객체 설정
        PostListResponseDTO responseDTO = new PostListResponseDTO();
        responseDTO.setPosts(pagedPosts);
        responseDTO.setNumberOfElements(pagedPosts.size());
        responseDTO.setPageNum(pageNum);
        responseDTO.setPageSize(pageSize);
        responseDTO.setTotalPages((int) Math.ceil((double) allPosts.size() / pageSize));
        responseDTO.setTotalElements((long) allPosts.size());

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);

    }

    @Operation(summary = "소셜 게시판 상세 조회", description = "socialPostId에 해당하는 게시글의 상세 조회 API 입니다")
    @GetMapping("/socialPosts/{socialPostId}")
    public ResponseEntity<PostResponseDTO> getSocialPost(@PathVariable Long socialPostId) {
        PostResponseDTO post1 = new PostResponseDTO();
        post1.setId(socialPostId);
        post1.setContent("내용1");
        post1.setTitle("제목1");
        post1.setImageUrl("이미지1");
        post1.setCommentCount(3);
        post1.setLikeCount(3);
        post1.setViewCount(3);
        post1.setUserId(1L);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
        post1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        return ResponseEntity.status(HttpStatus.OK).body(post1);
    }

    @Operation(summary = "유저의 게시판 전체 조회", description = "userId에 해당하는 게시글을 불러옵니다")
    @GetMapping("/users/{userId}/socialPosts")
    public ResponseEntity<UserPostListResponseDTO> getSocialPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "3") int pageSize) {
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
        responseDTO.setNumberOfElements(3);
        responseDTO.setTotalPages(1);
        responseDTO.setTotalElements(3L);

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
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
