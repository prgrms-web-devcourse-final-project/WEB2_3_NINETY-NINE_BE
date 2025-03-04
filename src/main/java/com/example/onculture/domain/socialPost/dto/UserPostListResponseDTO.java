package com.example.onculture.domain.socialPost.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserPostListResponseDTO {
    private List<PostWithLikeResponseDTO> posts;
    private int totalPages;
    private long totalElements;
    private int pageNum;
    private int pageSize;
    private int numberOfElements;
}
