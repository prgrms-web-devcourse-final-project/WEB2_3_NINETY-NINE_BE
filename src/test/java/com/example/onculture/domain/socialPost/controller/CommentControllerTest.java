package com.example.onculture.domain.socialPost.controller;

import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.service.CommentService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.domain.socialPost.domain.Comment;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.domain.Role;
import com.example.onculture.domain.user.domain.Social;
import com.example.onculture.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private CommentListResponseDTO mockCommentListResponseDTO;
    private CommentResponseDTO mockCommentResponseDTO;

    @BeforeEach
    void setUp() {

        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("TestUser")
                .role(Role.USER)
                .social(Social.Local)
                .build();
        Profile testProfile = Profile.builder().build();
        testProfile.setUser(testUser);
        testUser.setProfile(testProfile);

        SocialPost testSocialPost = SocialPost.builder()
                .id(1L)
                .user(testUser)
                .title("Test Post")
                .content("Test Content")
                .imageUrl("post.jpg")
                .build();

        Comment dummyComment = Comment.builder()
                .id(1L)
                .socialPost(testSocialPost)
                .user(testUser)
                .content("댓글 내용")
                .build();

        mockCommentResponseDTO = new CommentResponseDTO(dummyComment);

        mockCommentListResponseDTO = CommentListResponseDTO.builder()
                .comments(Arrays.asList(mockCommentResponseDTO))
                .totalPages(1)
                .pageNum(0)
                .pageSize(9)
                .totalElements(1)
                .numberOfElements(1)
                .build();
    }

    @Test
    @DisplayName("getCommentsByPost - 댓글 전체 조회 요청")
    void testGetCommentsByPost() {
        // given
        Long socialPostId = 1L;
        int pageNum = 0;
        int pageSize = 9;
        when(commentService.getCommentsByPost(pageNum, pageSize, socialPostId)).thenReturn(mockCommentListResponseDTO);

        // when
        ResponseEntity<SuccessResponse<CommentListResponseDTO>> response =
                commentController.getCommentsByPost(socialPostId, pageNum, pageSize);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCommentListResponseDTO, response.getBody().getData());
        verify(commentService, times(1)).getCommentsByPost(pageNum, pageSize, socialPostId);
    }

    @Test
    @DisplayName("createCommentByPost - 댓글 생성 요청")
    void testCreateCommentByPost() {
        // given
        Long socialPostId = 1L;
        CreateCommentRequestDTO requestDTO = new CreateCommentRequestDTO();
        requestDTO.setContent("댓글 내용");
        when(commentService.createCommentByPost(1L, socialPostId, requestDTO)).thenReturn(mockCommentResponseDTO);

        // when
        ResponseEntity<SuccessResponse<CommentResponseDTO>> response =
                commentController.createCommentByPost(socialPostId, requestDTO);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockCommentResponseDTO, response.getBody().getData());
        verify(commentService, times(1)).createCommentByPost(1L, socialPostId, requestDTO);
    }

    @Test
    @DisplayName("updateCommentByPost - 댓글 수정 요청")
    void testUpdateCommentByPost() {
        // given
        Long socialPostId = 1L;
        Long commentId = 1L;
        UpdateCommentRequestDTO requestDTO = new UpdateCommentRequestDTO();
        requestDTO.setContent("수정된 댓글 내용");
        when(commentService.updateCommentByPost(1L, socialPostId, commentId, requestDTO)).thenReturn(mockCommentResponseDTO);

        // when
        ResponseEntity<SuccessResponse<CommentResponseDTO>> response =
                commentController.updateCommentByPost(socialPostId, commentId, requestDTO);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCommentResponseDTO, response.getBody().getData());
        verify(commentService, times(1)).updateCommentByPost(1L, socialPostId, commentId, requestDTO);
    }

    @Test
    @DisplayName("deleteCommentByPost - 댓글 삭제 요청")
    void testDeleteCommentByPost() {
        // given
        Long socialPostId = 1L;
        Long commentId = 1L;
        String expectedResult = "삭제 완료";
        when(commentService.deleteCommentByPost(1L, socialPostId, commentId)).thenReturn(expectedResult);

        // when
        ResponseEntity<SuccessResponse<String>> response =
                commentController.deleteCommentByPost(socialPostId, commentId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody().getData());
        verify(commentService, times(1)).deleteCommentByPost(1L, socialPostId, commentId);
    }
}
