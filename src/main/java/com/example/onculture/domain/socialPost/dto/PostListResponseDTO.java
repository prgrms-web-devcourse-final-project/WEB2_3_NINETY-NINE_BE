package com.example.onculture.domain.socialPost.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PostListResponseDTO {
    private List<PostResponseDTO> posts;
    private int totalPages;
    private long totalElements;
    private int pageNum;
    private int pageSize;
    private int numberOfElements;
}
