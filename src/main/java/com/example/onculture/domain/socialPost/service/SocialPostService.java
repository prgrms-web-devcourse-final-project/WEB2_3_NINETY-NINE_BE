package com.example.onculture.domain.socialPost.service;


import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.image.ImageUrlUtil;
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
    private final SocialPostLikeRepository socialPostLikeRepository;

    public PostListResponseDTO getSocialPosts(String sort, int pageNum, int pageSize) {
        if (!(sort.equals("latest") || sort.equals("comments") || sort.equals("popular"))) {
            throw new CustomException(ErrorCode.INVALID_SORT_REQUEST);
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

    public PostWithLikeResponseDTO getSocialPostWithLikeStatus(Long socialPostId, Long userId) {
        SocialPost socialPost = socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        socialPost.increaseViewCount();

        socialPostRepository.save(socialPost);

        if (userId != null) {
            User user = findUserOrThrow(userId);
            boolean likeStatus = socialPostLikeRepository.existsByUserAndSocialPost(user,socialPost);
            return new PostWithLikeResponseDTO(socialPost, likeStatus);
        }
        else {
            return new PostWithLikeResponseDTO(socialPost, false);
        }
    }

    public PostResponseDTO createSocialPost(Long userId, CreatePostRequestDTO requestDTO) {
        User user = findUserOrThrow(userId);

        SocialPost socialPost = SocialPost.builder()
                .user(user)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .imageUrls(ImageUrlUtil.joinImageUrls(requestDTO.getImageUrls()))
                .build();

        socialPostRepository.save(socialPost);

        return new PostResponseDTO(socialPost);
    }

    public PostResponseDTO updateSocialPost(Long userId, UpdatePostRequestDTO requestDTO, Long socialPostId) {
        User user = findUserOrThrow(userId);

        validateOwner(socialPostId, user);

        SocialPost socialPost = socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        socialPost.updateSocialPost(requestDTO);

        socialPostRepository.save(socialPost);

        return new PostResponseDTO(socialPost);
    }

    public String deleteSocialPost(Long userId, Long socialPostId) {
        User user = findUserOrThrow(userId);

        validateOwner(socialPostId, user);

        socialPostRepository.deleteById(socialPostId);

        return "삭제 완료";
    }

    private void validatePageInput(int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateOwner(Long socialPostId, User user) {
        SocialPost socialPost = socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!(socialPost.getUser() == user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_MANAGE);
        }
    }
}
