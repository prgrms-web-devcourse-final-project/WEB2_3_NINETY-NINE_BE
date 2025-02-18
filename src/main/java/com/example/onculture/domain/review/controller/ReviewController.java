package com.example.onculture.domain.review.controller;



import com.example.onculture.domain.review.dto.CreateReviewRequestDTO;
import com.example.onculture.domain.review.dto.ReviewResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {
    @Operation(summary = "이벤트 후기 전체 조회", description = "eventId에 해당하는 포스트의 후기 전체 조회 API 입니다")
    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByEvent(@PathVariable Long eventId) {
        ReviewResponseDTO review1 = new ReviewResponseDTO();
        review1.setId(1L);
        review1.setUserId(1L);
        review1.setContent("내용1");
        review1.setEventId(eventId);
        review1.setStarRating(5);
        review1.setUserNickName("유저 닉네임1");
        review1.setImageUrl("super long url");
        review1.setCreatedAt(LocalDateTime.now().minusDays(1));

        ReviewResponseDTO review2 = new ReviewResponseDTO();
        review2.setId(2L);
        review2.setUserId(2L);
        review2.setContent("내용2");
        review2.setEventId(eventId);
        review2.setStarRating(5);
        review2.setUserNickName("유저 닉네임2");
        review2.setImageUrl("super long url");
        review2.setCreatedAt(LocalDateTime.now().minusDays(2));

        ReviewResponseDTO review3 = new ReviewResponseDTO();
        review3.setId(3L);
        review3.setUserId(3L);
        review3.setContent("내용3");
        review3.setEventId(eventId);
        review3.setStarRating(5);
        review3.setUserNickName("유저 닉네임3");
        review3.setImageUrl("super long url");
        review3.setCreatedAt(LocalDateTime.now().minusDays(3));

        List<ReviewResponseDTO> reviews = new ArrayList<>();
        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);

        return ResponseEntity.status(HttpStatus.OK).body(reviews);
    }

    @Operation(summary = "이벤트 후기 생성", description = "이벤트의 후기 생성 API 입니다")
    @PostMapping("/events/{eventId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReviewByEvent(
            @PathVariable Long eventId,
            @RequestBody CreateReviewRequestDTO requestDTO) {
        ReviewResponseDTO review = new ReviewResponseDTO();
        review.setId(1L);
        review.setUserId(1L);
        review.setContent(requestDTO.getContent());
        review.setEventId(eventId);
        review.setStarRating(5);
        review.setUserNickName("유저 닉네임1");
        review.setImageUrl(requestDTO.getImageUrl());
        review.setCreatedAt(LocalDateTime.now().minusDays(1));

        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "이벤트 후기 삭제", description = "eventId에 해당하는 포스트의 후기 삭제 API 입니다")
    @DeleteMapping("/events/{eventId}/reviews/{reviewId}")
    public ResponseEntity<String> deleteReviewByEvent(@PathVariable Long eventId, @PathVariable Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }
}
