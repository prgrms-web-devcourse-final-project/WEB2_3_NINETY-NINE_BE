package com.example.onculture.domain.review.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
public class ReviewResponseDTO {
    private Long id;
    private Long userId;
    private Long eventId;
    private String userNickName;
    private int starRating;
    private String imageUrl;
    private String content;
    private LocalDateTime createdAt;
}
