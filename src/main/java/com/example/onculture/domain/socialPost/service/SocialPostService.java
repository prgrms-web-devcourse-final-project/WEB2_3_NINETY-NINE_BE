package com.example.onculture.domain.socialPost.service;


import static com.example.onculture.global.utils.AwsS3Util.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.S3.S3Service;
import com.example.onculture.global.utils.image.ImageUrlUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
public class SocialPostService {
    private final UserRepository userRepository;
    private final SocialPostRepository socialPostRepository;
    private final SocialPostLikeRepository socialPostLikeRepository;
    private final S3Service s3Service;
    private final AmazonS3 amazonS3;



    public PostListResponseDTO getSocialPosts(String sort, int pageNum, int pageSize, Long userId) {
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

        Page<PostWithLikeResponseDTO> posts = socialPostRepository.findAll(pageable).map(socialPost -> {
            boolean likeStatus = userId != null &&
                    socialPostLikeRepository.existsByUserIdAndSocialPostId(userId, socialPost.getId());
            return new PostWithLikeResponseDTO(socialPost, likeStatus);
        });

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

    // 수정
    public PostWithLikeResponseDTO createSocialPost(Long userId, CreatePostRequestDTO requestDTO, List<MultipartFile> images) {
        User user = findUserOrThrow(userId);

        // 최대 4장의 이미지를 S3에 업로드
        List<String> uploadedImageUrls = images != null && !images.isEmpty()
            ? s3Service.uploadFiles(images, "social_posts")
            : Collections.emptyList();

        SocialPost socialPost = SocialPost.builder()
            .user(user)
            .title(requestDTO.getTitle())
            .content(requestDTO.getContent())
            .imageUrls(uploadedImageUrls)
            .build();

        socialPostRepository.save(socialPost);
        return new PostWithLikeResponseDTO(socialPost, false);
    }

    // 수정
    public PostWithLikeResponseDTO updateSocialPost(Long userId, UpdatePostRequestDTO requestDTO, Long socialPostId, List<MultipartFile> images) {
        User user = findUserOrThrow(userId);
        validateOwner(socialPostId, user);

        SocialPost socialPost = socialPostRepository.findById(socialPostId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // ✅ 기존 이미지 삭제
        deleteExistingImages(socialPost.getImageUrls());

        // ✅ 새 이미지 업로드
        List<String> uploadedImageUrls = images != null && !images.isEmpty()
            ? s3Service.uploadFiles(images, "social_posts")
            : Collections.emptyList();

        socialPost.updateSocialPost(requestDTO, uploadedImageUrls);
        socialPostRepository.save(socialPost);

        boolean likeStatus = socialPostLikeRepository.existsByUserIdAndSocialPostId(userId, socialPost.getId());
        return new PostWithLikeResponseDTO(socialPost, likeStatus);
    }


    // 수정
    public String deleteSocialPost(Long userId, Long socialPostId) {
        User user = findUserOrThrow(userId);
        validateOwner(socialPostId, user);

        SocialPost socialPost = socialPostRepository.findById(socialPostId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 게시글 삭제 시 이미지도 삭제
        deleteExistingImages(socialPost.getImageUrls());

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

    //추가
    private void deleteExistingImages(List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                try {
                    // ✅ URL 디코딩 적용 (한글, 공백, 특수문자 포함)
                    String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
                    String fileName = decodedUrl.substring(decodedUrl.lastIndexOf("/") + 1);

                    log.info("🟢 S3 삭제 요청 Key: {}", fileName);

                    // ✅ 정확한 폴더 경로 전달
                    s3Service.deleteFile("social_posts", fileName);

                    log.info("✅ S3 파일 삭제 완료: {}", fileName);
                } catch (Exception e) {
                    log.error("❌ S3 파일 삭제 실패: {}", imageUrl, e);
                }
            }
        }
    }



}
