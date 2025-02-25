package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.SocialPostLikeService;
import com.example.onculture.domain.socialPost.service.SocialPostService;
import com.example.onculture.global.response.SuccessResponse;
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
import java.util.Arrays;

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
    private PostResponseDTO mockPostResponseDTO;
    private UserPostListResponseDTO mockUserPostListResponseDTO;

    @BeforeEach
    void setUp() {

        mockPostResponseDTO = new PostResponseDTO(
                1L, 1L, "제목", "내용", "image.jpg",
                0, 0, 0,
                "닉네임", "profile.jpg",
                LocalDateTime.now(), LocalDateTime.now()
        );

        mockPostListResponseDTO = PostListResponseDTO.builder()
                .posts(Arrays.asList(mockPostResponseDTO))
                .totalPages(1)
                .totalElements(1)
                .pageNum(0)
                .pageSize(9)
                .numberOfElements(1)
                .build();

        mockUserPostListResponseDTO = UserPostListResponseDTO.builder()
                .posts(Arrays.asList(mockPostResponseDTO))
                .totalPages(1)
                .totalElements(1)
                .pageNum(0)
                .pageSize(9)
                .numberOfElements(1)
                .build();
    }

    @Test
    @DisplayName("소셜 게시판 전체 조회 - 기본 정렬 (latest) 요청")
    void testGetSocialPosts() {
        // given
        String sort = "latest";
        int pageNum = 0;
        int pageSize = 9;
        when(socialPostService.getSocialPosts(sort, pageNum, pageSize)).thenReturn(mockPostListResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostListResponseDTO>> response = socialPostController.getSocialPosts(sort, pageNum, pageSize);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostListResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).getSocialPosts(sort, pageNum, pageSize);
    }

    @Test
    @DisplayName("소셜 게시판 상세 조회 요청")
    void testGetSocialPost() {
        // given
        Long socialPostId = 1L;
        when(socialPostService.getSocialPost(socialPostId)).thenReturn(mockPostResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostResponseDTO>> response = socialPostController.getSocialPost(socialPostId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).getSocialPost(socialPostId);
    }

    @Test
    @DisplayName("유저의 게시판 전체 조회 요청")
    void testGetSocialPostsByUser() {
        // given
        Long userId = 1L;
        int pageNum = 0;
        int pageSize = 9;
        when(socialPostService.getSocialPostsByUser(userId, pageNum, pageSize)).thenReturn(mockUserPostListResponseDTO);

        // when
        ResponseEntity<SuccessResponse<UserPostListResponseDTO>> response = socialPostController.getSocialPostsByUser(userId, pageNum, pageSize);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUserPostListResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).getSocialPostsByUser(userId, pageNum, pageSize);
    }

    @Test
    @DisplayName("소셜 게시판 생성 요청")
    void testCreateSocialPost() {
        // given
        CreatePostRequestDTO requestDTO = new CreatePostRequestDTO();
        requestDTO.setTitle("제목");
        requestDTO.setContent("내용");
        requestDTO.setImageUrl("image.jpg");

        when(socialPostService.createSocialPost(1L, requestDTO)).thenReturn(mockPostResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostResponseDTO>> response = socialPostController.createSocialPost(requestDTO);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockPostResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).createSocialPost(1L, requestDTO);
    }

    @Test
    @DisplayName("소셜 게시판 수정 요청")
    void testUpdateSocialPost() {
        // given
        Long socialPostId = 1L;
        UpdatePostRequestDTO requestDTO = new UpdatePostRequestDTO();
        requestDTO.setTitle("수정 제목");
        requestDTO.setContent("수정 내용");
        requestDTO.setImageUrl("updated_image.jpg");

        when(socialPostService.updateSocialPost(1L, requestDTO, socialPostId)).thenReturn(mockPostResponseDTO);

        // when
        ResponseEntity<SuccessResponse<PostResponseDTO>> response = socialPostController.updateSocialPost(requestDTO, socialPostId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPostResponseDTO, response.getBody().getData());
        verify(socialPostService, times(1)).updateSocialPost(1L, requestDTO, socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 삭제 요청")
    void testDeleteSocialPost() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "삭제 완료";
        when(socialPostService.deleteSocialPost(1L, socialPostId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response = socialPostController.deleteSocialPost(socialPostId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostService, times(1)).deleteSocialPost(1L, socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 좋아요 토글 요청 - 좋아요 추가일 경우")
    void testToggleLike_Add() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "좋아요 추가";
        when(socialPostLikeService.toggleLike(1L, socialPostId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response = socialPostController.toggleLike(socialPostId);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostLikeService, times(1)).toggleLike(1L, socialPostId);
    }

    @Test
    @DisplayName("소셜 게시판 좋아요 토글 요청 - 좋아요 삭제일 경우")
    void testToggleLike_Remove() {
        // given
        Long socialPostId = 1L;
        String expectedResult = "좋아요 삭제";
        when(socialPostLikeService.toggleLike(1L, socialPostId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response = socialPostController.toggleLike(socialPostId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(socialPostLikeService, times(1)).toggleLike(1L, socialPostId);
    }
}
