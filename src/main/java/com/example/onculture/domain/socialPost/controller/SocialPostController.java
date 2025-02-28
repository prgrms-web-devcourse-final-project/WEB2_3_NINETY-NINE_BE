package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        PostResponseDTO post1 = new PostResponseDTO();
        post1.setId(1L);
        post1.setContent("내용1");
        post1.setTitle("제목1");
        post1.setImageUrl("이미지1");
        post1.setCommentCount(3);
        post1.setLikeCount(3);
        post1.setViewCount(3);
        post1.setUserId(1L);
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
        post2.setUserId(2L);
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
        post3.setUserId(3L);
        post3.setCreatedAt(LocalDateTime.now().minusDays(3));
        post3.setUpdatedAt(LocalDateTime.now().minusDays(3));

        PostResponseDTO post4 = new PostResponseDTO();
        post4.setId(4L);
        post4.setContent("내용4");
        post4.setTitle("제목4");
        post4.setImageUrl("이미지4");
        post4.setCommentCount(4);
        post4.setLikeCount(4);
        post4.setViewCount(4);
        post4.setUserId(4L);
        post4.setCreatedAt(LocalDateTime.now().minusDays(4));
        post4.setUpdatedAt(LocalDateTime.now().minusDays(4));

        PostResponseDTO post5 = new PostResponseDTO();
        post5.setId(5L);
        post5.setContent("내용5");
        post5.setTitle("제목5");
        post5.setImageUrl("이미지5");
        post5.setCommentCount(5);
        post5.setLikeCount(5);
        post5.setViewCount(5);
        post5.setUserId(5L);
        post5.setCreatedAt(LocalDateTime.now().minusDays(5));
        post5.setUpdatedAt(LocalDateTime.now().minusDays(5));

        PostResponseDTO post6 = new PostResponseDTO();
        post6.setId(6L);
        post6.setContent("내용6");
        post6.setTitle("제목6");
        post6.setImageUrl("이미지6");
        post6.setCommentCount(6);
        post6.setLikeCount(6);
        post6.setViewCount(6);
        post6.setUserId(6L);
        post6.setCreatedAt(LocalDateTime.now().minusDays(6));
        post6.setUpdatedAt(LocalDateTime.now().minusDays(6));

        PostResponseDTO post7 = new PostResponseDTO();
        post7.setId(7L);
        post7.setContent("내용7");
        post7.setTitle("제목7");
        post7.setImageUrl("이미지7");
        post7.setCommentCount(7);
        post7.setLikeCount(7);
        post7.setViewCount(7);
        post7.setUserId(7L);
        post7.setCreatedAt(LocalDateTime.now().minusDays(7));
        post7.setUpdatedAt(LocalDateTime.now().minusDays(7));

        PostResponseDTO post8 = new PostResponseDTO();
        post8.setId(8L);
        post8.setContent("내용8");
        post8.setTitle("제목8");
        post8.setImageUrl("이미지8");
        post8.setCommentCount(8);
        post8.setLikeCount(8);
        post8.setViewCount(8);
        post8.setUserId(8L);
        post8.setCreatedAt(LocalDateTime.now().minusDays(8));
        post8.setUpdatedAt(LocalDateTime.now().minusDays(8));

        PostResponseDTO post9 = new PostResponseDTO();
        post9.setId(9L);
        post9.setContent("내용9");
        post9.setTitle("제목9");
        post9.setImageUrl("이미지9");
        post9.setCommentCount(9);
        post9.setLikeCount(9);
        post9.setViewCount(9);
        post9.setUserId(9L);
        post9.setCreatedAt(LocalDateTime.now().minusDays(9));
        post9.setUpdatedAt(LocalDateTime.now().minusDays(9));

        PostResponseDTO post10 = new PostResponseDTO();
        post10.setId(10L);
        post10.setContent("내용10");
        post10.setTitle("제목10");
        post10.setImageUrl("이미지10");
        post10.setCommentCount(10);
        post10.setLikeCount(10);
        post10.setViewCount(10);
        post10.setUserId(10L);
        post10.setCreatedAt(LocalDateTime.now().minusDays(10));
        post10.setUpdatedAt(LocalDateTime.now().minusDays(10));

        PostResponseDTO post11 = new PostResponseDTO();
        post11.setId(11L);
        post11.setContent("내용11");
        post11.setTitle("제목11");
        post11.setImageUrl("이미지11");
        post11.setCommentCount(11);
        post11.setLikeCount(11);
        post11.setViewCount(11);
        post11.setUserId(11L);
        post11.setCreatedAt(LocalDateTime.now().minusDays(11));
        post11.setUpdatedAt(LocalDateTime.now().minusDays(11));

        PostResponseDTO post12 = new PostResponseDTO();
        post12.setId(12L);
        post12.setContent("내용12");
        post12.setTitle("제목12");
        post12.setImageUrl("이미지12");
        post12.setCommentCount(12);
        post12.setLikeCount(12);
        post12.setViewCount(12);
        post12.setUserId(12L);
        post12.setCreatedAt(LocalDateTime.now().minusDays(12));
        post12.setUpdatedAt(LocalDateTime.now().minusDays(12));

        List<PostResponseDTO> posts = new ArrayList<>();
        posts.add(post1);
        posts.add(post2);
        posts.add(post3);
        posts.add(post4);
        posts.add(post5);
        posts.add(post6);
        posts.add(post7);
        posts.add(post8);
        posts.add(post9);
        posts.add(post10);
        posts.add(post11);
        posts.add(post12);

        PostListResponseDTO responseDTO = new PostListResponseDTO();
        responseDTO.setPosts(posts);
        responseDTO.setNumberOfElements(12);
        responseDTO.setPageNum(pageNum);
        responseDTO.setPageSize(pageSize);
        responseDTO.setTotalPages(4); // 총 12개의 데이터가 있으므로 페이지는 4페이지
        responseDTO.setTotalElements(12L);

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
