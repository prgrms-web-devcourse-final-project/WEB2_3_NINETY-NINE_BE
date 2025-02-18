package com.example.onculture.domain.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequestDTO {
    private int starRating;
    private String imageUrl;
    private String content;
}
