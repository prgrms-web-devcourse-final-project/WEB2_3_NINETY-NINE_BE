package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.CommentResponseDTO;
import com.example.onculture.domain.socialPost.dto.CreateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {
    @Operation(summary = "소셜 게시판 댓글 전체 조회", description = "socialPostId에 해당하는 게시글의 댓글 전체 조회 API 입니다")
    @GetMapping("/socialPosts/{socialPostId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByPost(@PathVariable Long socialPostId) {
        CommentResponseDTO comment1 = new CommentResponseDTO();
        comment1.setId(1L);
        comment1.setUserId(1L);
        comment1.setContent("내용1");
        comment1.setSocialPostId(socialPostId);
        comment1.setCreatedAt(LocalDateTime.now().minusDays(3));
        comment1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        CommentResponseDTO comment2 = new CommentResponseDTO();
        comment2.setId(2L);
        comment2.setUserId(2L);
        comment2.setContent("내용2");
        comment2.setSocialPostId(socialPostId);
        comment2.setCreatedAt(LocalDateTime.now().minusDays(2));
        comment2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        CommentResponseDTO comment3 = new CommentResponseDTO();
        comment3.setId(3L);
        comment3.setUserId(3L);
        comment3.setContent("내용3");
        comment3.setSocialPostId(socialPostId);
        comment3.setCreatedAt(LocalDateTime.now().minusDays(1));
        comment3.setUpdatedAt(LocalDateTime.now().minusDays(1));

        List<CommentResponseDTO> comments = new ArrayList<>();
        comments.add(comment1);
        comments.add(comment2);
        comments.add(comment3);

        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }

    @Operation(summary = "소셜 게시판 댓글 생성", description = "socialPostId에 해당하는 게시글의 댓글 생성 API 입니다")
    @PostMapping("/socialPosts/{socialPostId}/comments")
    public ResponseEntity<CommentResponseDTO> createCommentByPost(
            @PathVariable Long socialPostId,
            @RequestBody CreateCommentRequestDTO requestDTO) {
        CommentResponseDTO comment1 = new CommentResponseDTO();
        comment1.setId(1L);
        comment1.setUserId(1L);
        comment1.setContent(requestDTO.getContent());
        comment1.setSocialPostId(socialPostId);
        comment1.setCreatedAt(LocalDateTime.now().minusDays(3));
        comment1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        return ResponseEntity.status(HttpStatus.CREATED).body(comment1);
    }

    @Operation(summary = "소셜 게시판 댓글 수정", description = "socialPostId에 해당하는 게시글의 댓글 생성 API 입니다")
    @PutMapping("/socialPosts/{socialPostId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateCommentByPost(
            @PathVariable Long socialPostId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequestDTO requestDTO) {
        CommentResponseDTO comment1 = new CommentResponseDTO();
        comment1.setId(commentId);
        comment1.setUserId(1L);
        comment1.setContent(requestDTO.getContent());
        comment1.setSocialPostId(socialPostId);
        comment1.setCreatedAt(LocalDateTime.now().minusDays(3));
        comment1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        return ResponseEntity.status(HttpStatus.OK).body(comment1);
    }

    @Operation(summary = "소셜 게시판 댓글 삭제", description = "socialPostId에 해당하는 게시글의 댓글 삭제 API 입니다")
    @DeleteMapping("/socialPosts/{socialPostId}/comments/{commentId}")
    public ResponseEntity<String> deleteCommentByPost(@PathVariable Long socialPostId, @PathVariable Long commentId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }
}
