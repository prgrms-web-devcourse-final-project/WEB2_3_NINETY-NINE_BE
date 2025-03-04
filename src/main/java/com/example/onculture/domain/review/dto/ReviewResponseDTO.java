package com.example.onculture.domain.review.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.onculture.domain.review.domain.Review;
import com.example.onculture.domain.review.domain.ReviewImage;

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
    private String eventTitle; // 이벤트 제목 (조회 시 추가)

    private Long userId;
    private String userNickname;
    private String content;
    private int rating;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponseDTO fromEntity(Review review, String eventTitle) {
        return ReviewResponseDTO.builder()
            .id(review.getId())
            .exhibitId(review.getExhibit() != null ? review.getExhibit().getSeq() : null)
            .festivalId(review.getFestival() != null ? review.getFestival().getId() : null)
            .performanceId(review.getPerformance() != null ? review.getPerformance().getId() : null)
            .popupStoreId(review.getPopupStore() != null ? review.getPopupStore().getId() : null)
            .eventTitle(eventTitle)
            .userId(review.getUser().getId())
            .userNickname(review.getUser().getNickname())
            .content(review.getContent())
            .rating(review.getRating())
            .imageUrls(review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList()))
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}

