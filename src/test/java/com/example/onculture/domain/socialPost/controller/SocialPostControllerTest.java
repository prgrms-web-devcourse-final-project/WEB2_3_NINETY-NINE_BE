package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.SocialPostLikeService;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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

    private ObjectMapper objectMapper;
    private PostListResponseDTO mockPostListResponseDTO;
    private PostWithLikeResponseDTO mockPostWithLikeResponseDTO;
    private UserPostListResponseDTO mockUserPostListResponseDTO;

    private CustomUserDetails customUserDetails;
    private List<MultipartFile> images;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        images = Arrays.asList(
            new MockMultipartFile("images", "image1.jpg", "image/jpeg", "dummy image 1".getBytes()),
            new MockMultipartFile("images", "image2.jpg", "image/jpeg", "dummy image 2".getBytes())
        );


        mockPostWithLikeResponseDTO = new PostWithLikeResponseDTO();
        mockPostWithLikeResponseDTO.setId(1L);
        mockPostWithLikeResponseDTO.setUserId(1L);
        mockPostWithLikeResponseDTO.setTitle("제목");
        mockPostWithLikeResponseDTO.setContent("내용");
        mockPostWithLikeResponseDTO.setImageUrls(Arrays.asList("image1.jpg", "image2.jpg"));
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
    void testCreateSocialPost() throws Exception {
        // given
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO();
        requestDTO.setTitle("제목");
        requestDTO.setContent("내용");

        String requestJson = objectMapper.writeValueAsString(requestDTO);
        MockMultipartFile requestDTOMultipart =
            new MockMultipartFile("requestDTO", "", "application/json", requestJson.getBytes());

        when(socialPostService.createSocialPost(
            eq(customUserDetails.getUserId()),
            any(CreatePostRequestDTO.class),  // ✅ 인스턴스 무시하고 타입만 체크
            eq(images)
        )).thenReturn(mockPostWithLikeResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> response =
            socialPostController.createSocialPost(requestJson, images, customUserDetails);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockPostWithLikeResponseDTO, response.getBody().getData());

        // ✅ 객체 매칭을 완화하여 검증
        verify(socialPostService, times(1)).createSocialPost(
            eq(customUserDetails.getUserId()),
            any(CreatePostRequestDTO.class),
            eq(images)
        );
    }


    @Test
    @DisplayName("소셜 게시판 수정 요청 - 인증 정보 포함")
    void testUpdateSocialPost() throws Exception {
        // given
        Long socialPostId = 1L;
        UpdatePostRequestDTO requestDTO = new UpdatePostRequestDTO();
        requestDTO.setTitle("수정 제목");
        requestDTO.setContent("수정 내용");

        String requestJson = objectMapper.writeValueAsString(requestDTO);
        MockMultipartFile requestDTOMultipart =
            new MockMultipartFile("requestDTO", "", "application/json", requestJson.getBytes());

        when(socialPostService.updateSocialPost(
            eq(customUserDetails.getUserId()),
            any(UpdatePostRequestDTO.class),  // ✅ 객체 매칭 완화
            eq(socialPostId),
            eq(images)
        )).thenReturn(mockPostWithLikeResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostWithLikeResponseDTO>> response =
            socialPostController.updateSocialPost(requestJson, images, socialPostId, customUserDetails);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostWithLikeResponseDTO, response.getBody().getData());

        // ✅ 객체 매칭을 완화하여 검증
        verify(socialPostService, times(1)).updateSocialPost(
            eq(customUserDetails.getUserId()),
            any(UpdatePostRequestDTO.class),
            eq(socialPostId),
            eq(images)
        );
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
