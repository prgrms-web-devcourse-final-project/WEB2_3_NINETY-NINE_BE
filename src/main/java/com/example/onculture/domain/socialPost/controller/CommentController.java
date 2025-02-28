package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.CommentListResponseDTO;
import com.example.onculture.domain.socialPost.dto.CommentResponseDTO;
import com.example.onculture.domain.socialPost.dto.CreateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import com.example.onculture.domain.socialPost.service.CommentService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Tag(name = "소셜 게시판 댓글 API", description = "소셜 게시판의 댓글을 관리하는 API")
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "소셜 게시판 댓글 전체 조회",
            description = "socialPostId에 해당하는 게시글의 댓글 전체 조회 API 입니다. pageNum과 pageSize의 기본값은 각각 0, 9입니다.")
    @GetMapping("/socialPosts/{socialPostId}/comments")
    public ResponseEntity<SuccessResponse<CommentListResponseDTO>> getCommentsByPost(
            @PathVariable Long socialPostId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "9") int pageSize) {
        CommentListResponseDTO responseDTO = commentService.getCommentsByPost(pageNum, pageSize, socialPostId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    @Operation(summary = "소셜 게시판 댓글 생성", description = "socialPostId에 해당하는 게시글의 댓글 생성 API 입니다")
    @PostMapping("/socialPosts/{socialPostId}/comments")
    public ResponseEntity<SuccessResponse<CommentResponseDTO>> createCommentByPost(
            @PathVariable Long socialPostId,
            @RequestBody CreateCommentRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponseDTO responseDTO = commentService.createCommentByPost(
                userDetails.getUserId(), socialPostId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.success(HttpStatus.CREATED, responseDTO));
    }

    @Operation(summary = "소셜 게시판 댓글 수정", description = "socialPostId에 해당하는 게시글의 댓글 생성 API 입니다")
    @PutMapping("/socialPosts/{socialPostId}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<CommentResponseDTO>> updateCommentByPost(
            @PathVariable Long socialPostId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponseDTO responseDTO = commentService.updateCommentByPost(
                userDetails.getUserId(), socialPostId, commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    @Operation(summary = "소셜 게시판 댓글 삭제", description = "socialPostId에 해당하는 게시글의 댓글 삭제 API 입니다")
    @DeleteMapping("/socialPosts/{socialPostId}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<String>> deleteCommentByPost(
            @PathVariable Long socialPostId, @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String result = commentService.deleteCommentByPost(
                userDetails.getUserId(), socialPostId, commentId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, result));
    }
}
