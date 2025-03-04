package com.example.onculture.domain.review.controller;



import com.example.onculture.domain.review.dto.ReviewRequestDTO;
import com.example.onculture.domain.review.dto.ReviewResponseDTO;
import com.example.onculture.domain.review.service.ReviewService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "공연 후기 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "공연 후기 작성", description = "현재 로그인한 사용자가 공연 후기를 작성합니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<ReviewResponseDTO>> createReview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReviewRequestDTO requestDTO) {

        ReviewResponseDTO createdReview = reviewService.createReview(userDetails.getUserId(), requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SuccessResponse.success("후기 작성 성공", createdReview));
    }

    @Operation(summary = "특정 이벤트(전시/축제/공연/팝업스토어)에 대한 후기 목록 조회",
        description = "이벤트 ID(exhibitId, festivalId, performanceId, popupStoreId) 중 하나를 제공하여 후기를 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<ReviewResponseDTO>>> getReviewsByEvent(
        @RequestParam(required = false) Long exhibitId,
        @RequestParam(required = false) Long festivalId,
        @RequestParam(required = false) Long performanceId,
        @RequestParam(required = false) Long popupStoreId) {

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByEvent(exhibitId, festivalId, performanceId, popupStoreId);
        return ResponseEntity.ok(SuccessResponse.success("후기 목록 조회 성공", reviews));
    }

    @Operation(summary = "후기 수정", description = "현재 로그인한 사용자가 자신의 후기를 수정합니다.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<ReviewResponseDTO>> updateReview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewRequestDTO requestDTO) {

        ReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, userDetails.getUserId(), requestDTO);

        return ResponseEntity.ok(SuccessResponse.success("후기 수정 성공", updatedReview));
    }

    @Operation(summary = "후기 삭제", description = "현재 로그인한 사용자가 자신의 후기를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<SuccessResponse<Void>> deleteReview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long reviewId) {

        reviewService.deleteReview(reviewId, userDetails.getUserId());
        return ResponseEntity.ok(SuccessResponse.success("후기 삭제 성공", null));
    }
}


