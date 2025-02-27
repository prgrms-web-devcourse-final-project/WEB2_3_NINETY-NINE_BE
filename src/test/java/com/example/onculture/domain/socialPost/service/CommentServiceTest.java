package com.example.onculture.domain.socialPost.service;


public class CommentServiceTest {

import com.example.onculture.domain.socialPost.domain.Comment;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.CommentListResponseDTO;
import com.example.onculture.domain.socialPost.dto.CommentResponseDTO;
import com.example.onculture.domain.socialPost.dto.CreateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import com.example.onculture.domain.socialPost.repository.CommentRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private SocialPostRepository socialPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private SocialPost testSocialPost;
    private Comment testComment;
    private CreateCommentRequestDTO createCommentRequestDTO;
    private UpdateCommentRequestDTO updateCommentRequestDTO;

    @BeforeEach
    void setUp() {

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("TestUser")
                .role(Role.USER)
                .build();

        Profile testProfile = Profile.builder().build();
        testProfile.setUser(testUser);
        testUser.setProfile(testProfile);

        testSocialPost = SocialPost.builder()
                .id(1L)
                .user(testUser)
                .title("Test Post")
                .content("Test Content")
                .imageUrl("post.jpg")
                .build();

        testComment = Comment.builder()
                .id(1L)
                .socialPost(testSocialPost)
                .user(testUser)
                .content("Original Comment")
                .build();

        createCommentRequestDTO = new CreateCommentRequestDTO();
        createCommentRequestDTO.setContent("New Comment");

        updateCommentRequestDTO = new UpdateCommentRequestDTO();
        updateCommentRequestDTO.setContent("Updated Comment");
    }

    @Test
    @DisplayName("getCommentsByPost - 정상 조회")
    void testGetCommentsByPost_valid() {
        // given
        int pageNum = 0;
        int pageSize = 9;
        Long socialPostId = testSocialPost.getId();
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment), pageable, 1);
        when(commentRepository.findBySocialPostId(eq(socialPostId), any(Pageable.class))).thenReturn(commentPage);

        // when
        CommentListResponseDTO dto = commentService.getCommentsByPost(pageNum, pageSize, socialPostId);

        // then
        assertNotNull(dto);
        assertEquals(1, dto.getComments().size());
        verify(socialPostRepository, times(1)).findById(socialPostId);
        verify(commentRepository, times(1)).findBySocialPostId(eq(socialPostId), any(Pageable.class));
    }

    @Test
    @DisplayName("createCommentByPost - 정상 생성")
    void testCreateCommentByPost_valid() {
        // given
        Long socialPostId = testSocialPost.getId();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            return Comment.builder()
                    .id(1L)
                    .socialPost(c.getSocialPost())
                    .user(c.getUser())
                    .content(c.getContent())
                    .build();
        });
        // when
        CommentResponseDTO dto = commentService.createCommentByPost(1L, socialPostId, createCommentRequestDTO);
        // then
        assertNotNull(dto);
        assertEquals("New Comment", dto.getContent());
        verify(userRepository, times(1)).findById(1L);
        verify(socialPostRepository, times(1)).findById(socialPostId);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(socialPostRepository, times(1)).save(testSocialPost);
    }

    @Test
    @DisplayName("updateCommentByPost - 정상 수정")
    void testUpdateCommentByPost_valid() {
        // given
        Long socialPostId = testSocialPost.getId();
        Long commentId = testComment.getId();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        // when
        CommentResponseDTO dto = commentService.updateCommentByPost(1L, socialPostId, commentId, updateCommentRequestDTO);
        // then
        assertNotNull(dto);
        verify(userRepository, times(1)).findById(1L);
        verify(commentRepository, times(2)).findById(commentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("deleteCommentByPost - 정상 삭제")
    void testDeleteCommentByPost_valid() {
        // given
        Long socialPostId = testSocialPost.getId();
        Long commentId = testComment.getId();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        // when
        String result = commentService.deleteCommentByPost(1L, socialPostId, commentId);
        // then
        assertEquals("삭제 완료", result);
        verify(commentRepository, times(1)).deleteById(commentId);
        verify(socialPostRepository, times(1)).save(testSocialPost);
    }

}
