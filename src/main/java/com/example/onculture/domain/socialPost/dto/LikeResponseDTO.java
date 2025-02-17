package com.example.onculture.domain.socialPost.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LikeResponseDTO {
    private Long id;
    private Long socialPostId;
    private Long userId;
    private LocalDateTime createdAt;
}
