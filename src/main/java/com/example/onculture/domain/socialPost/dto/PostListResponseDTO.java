package com.example.onculture.domain.socialPost.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostListResponseDTO {
    private List<PostResponseDTO> posts;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
