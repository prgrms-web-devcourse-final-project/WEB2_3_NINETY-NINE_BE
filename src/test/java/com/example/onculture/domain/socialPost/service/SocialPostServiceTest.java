package com.example.onculture.domain.socialPost.service;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.*;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SocialPostServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialPostRepository socialPostRepository;

    @Mock
    private SocialPostLikeRepository socialPostLikeRepository;

    @InjectMocks
    private SocialPostService socialPostService;

    private User testUser;
    private SocialPost testSocialPost;
    private CreatePostRequestDTO createPostRequestDTO;
    private UpdatePostRequestDTO updatePostRequestDTO;

    private List<String> images = new ArrayList<>();

    @BeforeEach
    void setUp() {
        images.add("image.jpg");
        images.add("image2.jpg");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("TestUser")
                .role(Role.USER)
                .build();

        Profile testProfile = new Profile();
        testProfile.setUser(testUser);
        testUser.setProfile(testProfile);

        testSocialPost = SocialPost.builder()
                .id(1L)
                .user(testUser)
                .title("제목")
                .content("내용")
                .imageUrls("image.jpg")
                .build();

        createPostRequestDTO = new CreatePostRequestDTO();
        createPostRequestDTO.setTitle("제목");
        createPostRequestDTO.setContent("내용");
        createPostRequestDTO.setImageUrls(images);

        updatePostRequestDTO = new UpdatePostRequestDTO();
        updatePostRequestDTO.setTitle("제목");
        updatePostRequestDTO.setContent("내용");
        updatePostRequestDTO.setImageUrls(images);
    }

    @Test
    @DisplayName("getSocialPosts - 'latest' 정렬 정상 처리")
    void testGetSocialPosts_validLatest() {
        // given
        String sort = "latest";
        int pageNum = 0;
        int pageSize = 9;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<SocialPost> page = new PageImpl<>(List.of(testSocialPost), pageable, 1);
        when(socialPostRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        PostListResponseDTO dto = socialPostService.getSocialPosts(sort, pageNum, pageSize);

        // then
        assertNotNull(dto);
        assertEquals(1, dto.getPosts().size());
        assertEquals(1, dto.getTotalElements());
        verify(socialPostRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getSocialPosts - 잘못된 정렬 옵션 시 예외 발생")
    void testGetSocialPosts_invalidSort() {
        // given
        String sort = "invalid";
        int pageNum = 0;
        int pageSize = 9;

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.getSocialPosts(sort, pageNum, pageSize)
        );
        assertEquals(ErrorCode.INVALID_SORT_REQUEST, ex.getErrorCode());
    }

    @Test
    @DisplayName("getSocialPosts - 음수 페이지 입력 시 예외 발생")
    void testGetSocialPosts_invalidPageInput() {
        // given
        String sort = "latest";
        int pageNum = -1;
        int pageSize = 9;

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.getSocialPosts(sort, pageNum, pageSize)
        );
        assertEquals(ErrorCode.INVALID_PAGE_REQUEST, ex.getErrorCode());
    }

    @Test
    @DisplayName("getSocialPost - 정상 조회")
    void testGetSocialPost_valid() {
        // given
        Long socialPostId = 1L;
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        when(socialPostLikeRepository.existsByUserAndSocialPost(testUser,testSocialPost)).thenReturn(true);

        // when
        PostWithLikeResponseDTO dto = socialPostService.getSocialPostWithLikeStatus(socialPostId, userId);

        // then
        assertNotNull(dto);
        verify(socialPostRepository, times(1)).findById(socialPostId);
        verify(socialPostRepository, times(1)).save(testSocialPost);
    }

    @Test
    @DisplayName("getSocialPost - 게시글 미존재 시 예외 발생")
    void testGetSocialPost_notFound() {
        // given
        Long socialPostId = 999L;
        Long userId = 1L;
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.getSocialPostWithLikeStatus(socialPostId, userId)
        );
        assertEquals(ErrorCode.POST_NOT_FOUND, ex.getErrorCode());
    }

//    @Test
//    @DisplayName("getSocialPostsByUser - 정상 조회")
//    void testGetSocialPostsByUser_valid() {
//        // given
//        Long userId = 1L;
//        int pageNum = 0;
//        int pageSize = 9;
//        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
//        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
//        Page<SocialPost> page = new PageImpl<>(List.of(testSocialPost), pageable, 1);
//        when(socialPostRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);
//
//        // when
//        UserPostListResponseDTO dto = socialPostService.getSocialPostsByUser(userId, pageNum, pageSize);
//
//        // then
//        assertNotNull(dto);
//        assertEquals(1, dto.getPosts().size());
//        verify(userRepository, times(1)).findById(userId);
//        verify(socialPostRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
//    }

//    @Test
//    @DisplayName("getSocialPostsByUser - 사용자 미존재 시 예외 발생")
//    void testGetSocialPostsByUser_userNotFound() {
//        // given
//        Long userId = 999L;
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // when & then
//        CustomException ex = assertThrows(CustomException.class, () ->
//                socialPostService.getSocialPostsByUser(userId, 0, 10)
//        );
//        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
//    }

    @Test
    @DisplayName("createSocialPost - 정상 생성")
    void testCreateSocialPost_valid() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.save(any(SocialPost.class))).thenAnswer(invocation -> {
            SocialPost sp = invocation.getArgument(0);
            return SocialPost.builder()
                    .id(1L)
                    .user(sp.getUser())
                    .title(sp.getTitle())
                    .content(sp.getContent())
                    .imageUrls(sp.getImageUrls())
                    .build();
        });

        // when
        PostResponseDTO dto = socialPostService.createSocialPost(userId, createPostRequestDTO);

        // then
        assertNotNull(dto);
        verify(userRepository, times(1)).findById(userId);
        verify(socialPostRepository, times(1)).save(any(SocialPost.class));
    }

    @Test
    @DisplayName("createSocialPost - 사용자 미존재 시 예외 발생")
    void testCreateSocialPost_userNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.createSocialPost(userId, createPostRequestDTO)
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateSocialPost - 정상 수정")
    void testUpdateSocialPost_valid() {
        // given
        Long userId = 1L;
        Long socialPostId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));
        when(socialPostRepository.save(any(SocialPost.class))).thenReturn(testSocialPost);

        // when
        PostResponseDTO dto = socialPostService.updateSocialPost(userId, updatePostRequestDTO, socialPostId);

        // then
        assertNotNull(dto);
        verify(userRepository, times(1)).findById(userId);
        verify(socialPostRepository, times(2)).findById(socialPostId);
        verify(socialPostRepository, times(1)).save(testSocialPost);
    }

    @Test
    @DisplayName("updateSocialPost - 게시글 미존재 시 예외 발생")
    void testUpdateSocialPost_postNotFound() {
        // given
        Long userId = 1L;
        Long socialPostId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.updateSocialPost(userId, updatePostRequestDTO, socialPostId)
        );
        assertEquals(ErrorCode.POST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateSocialPost - 소유권 위반 시 예외 발생")
    void testUpdateSocialPost_unauthorized() {
        // given
        Long userId = 1L;
        Long socialPostId = 1L;
        // given: 다른 사용자가 소유한 게시글 생성
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("password")
                .nickname("Other")
                .role(Role.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        SocialPost otherPost = SocialPost.builder()
                .id(socialPostId)
                .user(otherUser)
                .title("Title")
                .content("Content")
                .imageUrls("img.jpg")
                .build();
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(otherPost));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.updateSocialPost(userId, updatePostRequestDTO, socialPostId)
        );
        assertEquals(ErrorCode.UNAUTHORIZED_POST_MANAGE, ex.getErrorCode());
    }

    @Test
    @DisplayName("deleteSocialPost - 정상 삭제")
    void testDeleteSocialPost_valid() {
        // given
        Long userId = 1L;
        Long socialPostId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(testSocialPost));

        // when
        String result = socialPostService.deleteSocialPost(userId, socialPostId);

        // then
        assertEquals("삭제 완료", result);
        verify(socialPostRepository, times(1)).deleteById(socialPostId);
    }

    @Test
    @DisplayName("deleteSocialPost - 소유권 위반 시 삭제 실패")
    void testDeleteSocialPost_unauthorized() {
        // given
        Long userId = 1L;
        Long socialPostId = 1L;
        // given: 다른 사용자가 소유한 게시글 생성
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("password")
                .nickname("Other")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        SocialPost otherPost = SocialPost.builder()
                .id(socialPostId)
                .user(otherUser)
                .title("Title")
                .content("Content")
                .imageUrls("img.jpg")
                .build();
        when(socialPostRepository.findById(socialPostId)).thenReturn(Optional.of(otherPost));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                socialPostService.deleteSocialPost(userId, socialPostId)
        );
        assertEquals(ErrorCode.UNAUTHORIZED_POST_MANAGE, ex.getErrorCode());
    }
}
