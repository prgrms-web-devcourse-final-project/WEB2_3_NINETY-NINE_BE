package com.example.onculture.domain.socialPost.service;


import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final SocialPostRepository socialPostRepository;

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

    public PostResponseDTO getSocialPost(Long socialPostId) {
        return socialPostRepository
                .findById(socialPostId)
                .map(PostResponseDTO::new)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    public UserPostListResponseDTO getSocialPostsByUser(Long userId, int pageNum, int pageSize) {
        existsByUserId(userId);

        validatePageInput(pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<PostResponseDTO> posts = socialPostRepository.findByUserId(userId, pageable).map(PostResponseDTO::new);

        return UserPostListResponseDTO.builder()
                .posts(posts.getContent())
                .totalPages(posts.getTotalPages())
                .pageNum(posts.getNumber())
                .pageSize(posts.getSize())
                .totalElements(posts.getTotalElements())
                .numberOfElements(posts.getNumberOfElements())
                .build();
    }

    public PostResponseDTO createSocialPost(Long userId, CreatePostRequestDTO requestDTO) {
        existsByUserId(userId);

        SocialPost socialPost = SocialPost.builder()
                .userId(userId)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .imageUrl(requestDTO.getImageUrl())
                .build();

        socialPostRepository.save(socialPost);

        return new PostResponseDTO(socialPost);
    }

    public PostResponseDTO updateSocialPost(Long userId, UpdatePostRequestDTO requestDTO, Long socialPostId) {
        existsByUserId(userId);

        validateOwner(socialPostId, userId);

        SocialPost socialPost = socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        socialPost.updateSocialPost(requestDTO);

        socialPostRepository.save(socialPost);

        return new PostResponseDTO(socialPost);
    }

    public String deleteSocialPost(Long userId, Long socialPostId) {
        existsByUserId(userId);

        validateOwner(socialPostId, userId);

        socialPostRepository.deleteById(socialPostId);

        return "삭제 완료";
    }

    private void validatePageInput(int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    private void existsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateOwner(Long socialPostId, Long userId) {
        SocialPost socialPost = socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!socialPost.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_MANAGE);
        }
    }
}
