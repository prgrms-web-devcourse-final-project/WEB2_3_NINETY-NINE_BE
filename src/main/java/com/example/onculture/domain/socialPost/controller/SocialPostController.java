package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.CreatePostRequestDTO;
import com.example.onculture.domain.socialPost.dto.LikeResponseDTO;
import com.example.onculture.domain.socialPost.dto.PostResponseDTO;
import com.example.onculture.domain.socialPost.dto.UpdatePostRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SocialPostController {
    @GetMapping("/socialPosts")
    public ResponseEntity<List<PostResponseDTO>> getSocialPosts(@RequestParam(defaultValue = "latest") String sort,
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

        List<PostResponseDTO> posts = new ArrayList<>();
        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

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

    @GetMapping("/users/{userId}/socialPosts")
    public ResponseEntity<List<PostResponseDTO>> getSocialPostsByUser(
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

        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

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

    @DeleteMapping("/socialPosts/{socialPostId}")
    public ResponseEntity<String> deleteSocialPost(@PathVariable Long socialPostId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }

    @PostMapping("/socialPosts/{socialPostId}/likes")
    public ResponseEntity<LikeResponseDTO> addLike(@PathVariable Long socialPostId) {
        LikeResponseDTO like = new LikeResponseDTO();
        like.setId(1L);
        like.setSocialPostId(socialPostId);
        like.setUserId(1L);
        like.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(like);
    }

    @DeleteMapping("/socialPosts/{socialPostId}/likes/{likeId}")
    public ResponseEntity<String> deleteLikeBySocialPost(@PathVariable Long socialPostId, @PathVariable Long likeId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }
}
