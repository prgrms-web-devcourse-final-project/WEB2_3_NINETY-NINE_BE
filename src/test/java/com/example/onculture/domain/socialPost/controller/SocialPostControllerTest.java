package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.SocialPostLikeService;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SocialPostControllerTest {

    @Mock
    private SocialPostService socialPostService;

    @Mock
    private SocialPostLikeService socialPostLikeService;

    @InjectMocks
    private SocialPostController socialPostController;

    private PostListResponseDTO mockPostListResponseDTO;
    private PostWithLikeResponseDTO mockPostWithLikeResponseDTO;
    private UserPostListResponseDTO mockUserPostListResponseDTO;

    private CustomUserDetails customUserDetails;
    private List<String> images = new ArrayList<>();

    @BeforeEach
    void setUp() {
        images.add("image.jpg");
        images.add("image2.jpg");

        mockPostWithLikeResponseDTO = new PostWithLikeResponseDTO();
        mockPostWithLikeResponseDTO.setId(1L);
        mockPostWithLikeResponseDTO.setUserId(1L);
        mockPostWithLikeResponseDTO.setTitle("제목");
        mockPostWithLikeResponseDTO.setContent("내용");
        mockPostWithLikeResponseDTO.setImageUrls(images);
        mockPostWithLikeResponseDTO.setViewCount(0);
        mockPostWithLikeResponseDTO.setCommentCount(0);
        mockPostWithLikeResponseDTO.setLikeCount(0);
        mockPostWithLikeResponseDTO.setUserNickname("닉네임");
        mockPostWithLikeResponseDTO.setUserProfileImage("profile.jpg");
        mockPostWithLikeResponseDTO.setLikeStatus(false);
        mockPostWithLikeResponseDTO.setCreatedAt(LocalDateTime.now());
        mockPostWithLikeResponseDTO.setUpdatedAt(LocalDateTime.now());

        mockPostListResponseDTO = PostListResponseDTO.builder()
                .posts(Arrays.asList(mockPostWithLikeResponseDTO))
                .totalPages(1)
                .totalElements(1)
                .pageNum(0)
                .pageSize(9)
                .numberOfElements(1)
                .build();

        mockUserPostListResponseDTO = UserPostListResponseDTO.builder()
                .posts(Arrays.asList(mockPostWithLikeResponseDTO))
                .totalPages(1)
                .totalElements(1)
                .pageNum(0)
                .pageSize(9)
                .numberOfElements(1)
                .build();

        customUserDetails = CustomUserDetails.builder()
                .userId(1L)
                .build();
    }

    @Test
    @DisplayName("소셜 게시판 전체 조회 - 기본 정렬 (latest) 요청")
    void testGetSocialPosts() {
        // given
        String sort = "latest";
        int pageNum = 0;
        int pageSize = 9;
        Long userId = customUserDetails.getUserId();
        when(socialPostService.getSocialPosts(sort, pageNum, pageSize, userId))
                .thenReturn(mockPostListResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostListResponseDTO>> response =
                socialPostController.getSocialPosts(sort, pageNum, pageSize, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostListResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).getSocialPosts(sort, pageNum, pageSize, userId);
    }

    @Test
    @DisplayName("소셜 게시판 상세 조회 요청")
    void testGetSocialPost() {
        // given
        Long socialPostId = 1L;
        Long userId = customUserDetails.getUserId();
        when(socialPostService.getSocialPostWithLikeStatus(socialPostId, userId))
                .thenReturn(mockPostWithLikeResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> response =
                socialPostController.getSocialPost(socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostWithLikeResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).getSocialPostWithLikeStatus(socialPostId, userId);
    }

    @Test
    @DisplayName("소셜 게시판 생성 요청 - 인증 정보 포함")
    void testCreateSocialPost() {
        // given
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO();
        requestDTO.setTitle("제목");
        requestDTO.setContent("내용");
        requestDTO.setImageUrls(images);

        when(socialPostService.createSocialPost(customUserDetails.getUserId(), requestDTO))
                .thenReturn(mockPostWithLikeResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> response =
                socialPostController.createSocialPost(requestDTO, customUserDetails);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockPostWithLikeResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).createSocialPost(customUserDetails.getUserId(), requestDTO);
    }

    @Test
    @DisplayName("소셜 게시판 수정 요청 - 인증 정보 포함")
    void testUpdateSocialPost() {
        // given
        Long socialPostId = 1L;
        UpdatePostRequestDTO requestDTO = new UpdatePostRequestDTO();
        requestDTO.setTitle("수정 제목");
        requestDTO.setContent("수정 내용");
        requestDTO.setImageUrls(images);

        when(socialPostService.updateSocialPost(customUserDetails.getUserId(), requestDTO, socialPostId))
                .thenReturn(mockPostWithLikeResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> response =
                socialPostController.updateSocialPost(requestDTO, socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostWithLikeResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).updateSocialPost(customUserDetails.getUserId(), requestDTO, socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 삭제 요청 - 인증 정보 포함")
    void testDeleteSocialPost() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "삭제 완료";
        when(socialPostService.deleteSocialPost(customUserDetails.getUserId(), socialPostId))
                .thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response =
                socialPostController.deleteSocialPost(socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostService, times(1)).deleteSocialPost(customUserDetails.getUserId(), socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 좋아요 토글 요청 - 좋아요 추가, 인증 정보 포함")
    void testToggleLike_Add() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "좋아요 추가";
        when(socialPostLikeService.toggleLike(customUserDetails.getUserId(), socialPostId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response =
                socialPostController.toggleLike(socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostLikeService, times(1)).toggleLike(customUserDetails.getUserId(), socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 좋아요 토글 요청 - 좋아요 삭제, 인증 정보 포함")
    void testToggleLike_Remove() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "좋아요 삭제";
        when(socialPostLikeService.toggleLike(customUserDetails.getUserId(), socialPostId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response =
                socialPostController.toggleLike(socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostLikeService, times(1)).toggleLike(customUserDetails.getUserId(), socialPostId);
    }
}
