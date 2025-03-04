package com.example.onculture.domain.review.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long id;
    private Long exhibitId;
    private Long festivalId;
    private Long performanceId;
    private Long popupStoreId;

    private Long userId;
    private String userNickname;
    private String content;
    private int rating;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

