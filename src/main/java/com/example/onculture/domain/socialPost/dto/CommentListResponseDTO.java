package com.example.onculture.domain.socialPost.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentListResponseDTO {
    private List<CommentResponseDTO> comments;
    private int totalPages;
    private long totalElements;
    private int pageNum;
    private int pageSize;
    private int numberOfElements;
}
