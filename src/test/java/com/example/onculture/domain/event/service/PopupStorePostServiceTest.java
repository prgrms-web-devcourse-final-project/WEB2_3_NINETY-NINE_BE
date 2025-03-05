package com.example.onculture.domain.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class PopupStorePostServiceTest {

    @Mock
    private PopupStorePostRepository popupStorePostRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private PopupStorePostService popupStorePostService;

    // 기존 더미 PopupStorePost 생성 메서드 (imageUrls를 빈 리스트로 초기화)
    private PopupStorePost createDummyPopupStorePost(Long id) {
        PopupStorePost post = new PopupStorePost();
        post.setId(id);
        post.setContent("Dummy content " + id);
        post.setImageUrls(new ArrayList<>());
        return post;
    }

    // 검색 테스트용: content에 검색어를 포함하는 PopupStorePost 생성 메서드
    private PopupStorePost createDummyPopupStorePostWithContent(Long id, String content) {
        PopupStorePost post = new PopupStorePost();
        post.setId(id);
        post.setContent(content);
        post.setImageUrls(new ArrayList<>());
        return post;
    }

    // 더미 User 생성 메서드
    private User createDummyUser(Long id) {
        return User.builder()
                .id(id)
                .email("dummy@example.com")
                .password("password")
                .nickname("dummyUser")
                .role(Role.USER)
                .loginType(LoginType.LOCAL_ONLY)
                .socials(Set.of(Social.LOCAL))
                .build();
    }

    // 더미 Bookmark 생성 메서드
    private Bookmark createDummyBookmark(Long userId, PopupStorePost post) {
        return Bookmark.builder()
                .user(createDummyUser(userId))
                .popupStorePost(post)
                .build();
    }

    @BeforeEach
    void setUp() {
        // 초기화 작업이 필요하면 작성
    }

    @Test
    @DisplayName("listAll - 모든 PopupStorePost 조회")
    void testListAll() {
        PopupStorePost post1 = createDummyPopupStorePost(1L);
        PopupStorePost post2 = createDummyPopupStorePost(2L);
        when(popupStorePostRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        List<PopupStorePost> result = popupStorePostService.listAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    @DisplayName("searchByTitle - 제목으로 PopupStorePost 검색")
    void testSearchByTitle() {
        String title = "Test";
        // 검색 테스트용으로 content에 "Test"가 포함된 객체 생성
        PopupStorePost post = createDummyPopupStorePostWithContent(3L, "This is a Test content for popup store post");
        when(popupStorePostRepository.findByContentContaining(title))
                .thenReturn(Collections.singletonList(post));

        List<PopupStorePost> result = popupStorePostService.searchByTitle(title);

        assertNotNull(result);
        assertEquals(1, result.size());
        // 검색어가 포함되어 있는지 확인
        assertTrue(result.get(0).getContent().contains(title));
    }

    @Test
    @DisplayName("getRandomPopupStorePosts - 북마크 미등록 경우")
    void testGetRandomPopupStorePosts_NotBookmarked() {
        int randomSize = 2;
        Long userId = 100L;
        PopupStorePost post1 = createDummyPopupStorePost(10L);
        PopupStorePost post2 = createDummyPopupStorePost(11L);

        when(popupStorePostRepository.findRandomPopupStorePosts(randomSize))
                .thenReturn(Arrays.asList(post1, post2));
        when(bookmarkRepository.findByUserIdAndPopupStorePostId(userId, post1.getId()))
                .thenReturn(Optional.empty());
        when(bookmarkRepository.findByUserIdAndPopupStorePostId(userId, post2.getId()))
                .thenReturn(Optional.empty());

        List<EventResponseDTO> result = popupStorePostService.getRandomPopupStorePosts(randomSize, userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(dto -> assertFalse(dto.getBookmarked()));
    }

    @Test
    @DisplayName("getRandomPopupStorePosts - 북마크 등록 경우")
    void testGetRandomPopupStorePosts_Bookmarked() {
        int randomSize = 1;
        Long userId = 200L;
        PopupStorePost post = createDummyPopupStorePost(20L);

        when(popupStorePostRepository.findRandomPopupStorePosts(randomSize))
                .thenReturn(Collections.singletonList(post));
        Bookmark bookmark = createDummyBookmark(userId, post);
        when(bookmarkRepository.findByUserIdAndPopupStorePostId(userId, post.getId()))
                .thenReturn(Optional.of(bookmark));

        List<EventResponseDTO> result = popupStorePostService.getRandomPopupStorePosts(randomSize, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getBookmarked());
    }

    @Test
    @DisplayName("getRandomPopupStorePosts - 음수 randomSize 입력 시 예외 발생")
    void testGetRandomPopupStorePosts_InvalidInput() {
        int randomSize = -1;
        Long userId = 300L;

        CustomException exception = assertThrows(CustomException.class,
                () -> popupStorePostService.getRandomPopupStorePosts(randomSize, userId));
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("getPopupStorePostDetail - 북마크 미등록 경우")
    void testGetPopupStorePostDetail_NotBookmarked() {
        Long id = 40L;
        Long userId = 400L;
        PopupStorePost post = createDummyPopupStorePost(id);
        when(popupStorePostRepository.findById(id))
                .thenReturn(Optional.of(post));
        when(bookmarkRepository.findByUserIdAndPopupStorePostId(userId, post.getId()))
                .thenReturn(Optional.empty());

        EventResponseDTO result = popupStorePostService.getPopupStorePostDetail(id, userId);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertFalse(result.getBookmarked());
    }

    @Test
    @DisplayName("getPopupStorePostDetail - 북마크 등록 경우")
    void testGetPopupStorePostDetail_Bookmarked() {
        Long id = 50L;
        Long userId = 500L;
        PopupStorePost post = createDummyPopupStorePost(id);
        when(popupStorePostRepository.findById(id))
                .thenReturn(Optional.of(post));
        Bookmark bookmark = createDummyBookmark(userId, post);
        when(bookmarkRepository.findByUserIdAndPopupStorePostId(userId, post.getId()))
                .thenReturn(Optional.of(bookmark));

        EventResponseDTO result = popupStorePostService.getPopupStorePostDetail(id, userId);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertTrue(result.getBookmarked());
    }

    @Test
    @DisplayName("searchPopupStorePosts - 조건에 따른 PopupStorePost 검색")
    void testSearchPopupStorePosts() {
        String region = "서울특별시";
        String status = "진행중";
        String titleKeyword = "Test";
        int pageNum = 0;
        int pageSize = 5;
        Long userId = 600L;
        PopupStorePost post = createDummyPopupStorePost(60L);
        Page<PopupStorePost> page = new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(pageNum, pageSize),
                1
        );
        // 내부에서 bookmarkRepository.findByUserIdAndPerformanceId를 사용하므로 모의 처리
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, post.getId()))
                .thenReturn(Optional.empty());
        when(popupStorePostRepository.findAll((Specification<PopupStorePost>) any(), eq(PageRequest.of(pageNum, pageSize))))
                .thenReturn(page);

        EventPageResponseDTO response = popupStorePostService.searchPopupStorePosts(
                region, status, titleKeyword, pageNum, pageSize, userId
        );

        assertNotNull(response);
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals(pageNum, response.getPageNum());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(1, response.getNumberOfElements());
        response.getPosts().forEach(dto -> assertFalse(dto.getBookmarked()));
    }
}
