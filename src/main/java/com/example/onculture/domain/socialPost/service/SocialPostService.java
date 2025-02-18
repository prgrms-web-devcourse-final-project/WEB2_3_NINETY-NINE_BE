package com.example.onculture.domain.socialPost.service;


import com.example.onculture.domain.socialPost.dto.PostListResponseDTO;
import com.example.onculture.domain.socialPost.dto.PostResponseDTO;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SocialPostService {
    private SocialPostRepository socialPostRepository;

    public PostListResponseDTO getSocialPosts(String sort, int pageNum, int pageSize) {
        if (!(sort.equals("latest") || sort.equals("comments") || sort.equals("popular"))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validatePageInput(pageNum, pageSize);

        Sort sortConfig = Sort.by("createdAt").descending();

        if (sort.equals("comments")) {
            sortConfig = Sort.by("commentCount").descending();
        }
        if (sort.equals("popular")) {
            sortConfig = Sort.by("likeCount").descending();
        }

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortConfig);

        Page<PostResponseDTO> posts = socialPostRepository.findAll(pageable).map(PostResponseDTO::new);

        return PostListResponseDTO.builder()
                .posts(posts.getContent())
                .totalPages(posts.getTotalPages())
                .pageNum(posts.getNumber())
                .pageSize(posts.getSize())
                .totalElements(posts.getTotalElements())
                .numberOfElements(posts.getNumberOfElements())
                .build();
    }

    private void validatePageInput(int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }
}
