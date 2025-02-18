package com.example.onculture.domain.socialPost.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String imageUrl;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
