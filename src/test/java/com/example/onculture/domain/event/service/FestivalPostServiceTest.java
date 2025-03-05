package com.example.onculture.domain.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
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
public class FestivalPostServiceTest {

    @Mock
    private FestivalPostRepository festivalPostRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private FestivalPostService festivalPostService;

    // 더미 FestivalPost 생성 메서드
    private FestivalPost createDummyFestivalPost(Long id) {
        FestivalPost post = new FestivalPost();
        post.setId(id);
        post.setFestivalContent("Festival content " + id);
        // 시작일: 내일, 종료일: 모레로 설정 (테스트 용도)
        post.setFestivalStartDate(new java.sql.Date(System.currentTimeMillis() + 86400000));
        post.setFestivalEndDate(new java.sql.Date(System.currentTimeMillis() + 172800000));
        post.setFestivalLocation("Seoul");
        // imageUrls가 null이 되지 않도록 빈 리스트로 초기화
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
    private Bookmark createDummyBookmark(Long userId, FestivalPost post) {
        return Bookmark.builder()
                .user(createDummyUser(userId))
                .festivalPost(post)
                .build();
    }

    @BeforeEach
    void setUp() {
        // 추가 초기화가 필요하면 이곳에 작성
    }

    @Test
    @DisplayName("listAll - 모든 FestivalPost 조회")
    void testListAll() {
        // Given
        FestivalPost post1 = createDummyFestivalPost(1L);
        FestivalPost post2 = createDummyFestivalPost(2L);
        when(festivalPostRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        // When
        List<FestivalPost> result = festivalPostService.listAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    @DisplayName("searchByTitle - 제목으로 FestivalPost 검색")
    void testSearchByTitle() {
        // Given
        String title = "Festival";
        FestivalPost post = createDummyFestivalPost(3L);
        when(festivalPostRepository.findByFestivalContentContaining(title))
                .thenReturn(Collections.singletonList(post));

        // When
        List<FestivalPost> result = festivalPostService.searchByTitle(title);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFestivalContent().contains(title));
    }

    @Test
    @DisplayName("getRandomFestivalPosts - 북마크 미등록 경우")
    void testGetRandomFestivalPosts_NotBookmarked() {
        // Given
        int randomSize = 2;
        Long userId = 100L;
        FestivalPost post1 = createDummyFestivalPost(10L);
        FestivalPost post2 = createDummyFestivalPost(11L);

        when(festivalPostRepository.findRandomFestivalPosts(randomSize))
                .thenReturn(Arrays.asList(post1, post2));
        when(bookmarkRepository.findByUserIdAndFestivalPostId(userId, post1.getId()))
                .thenReturn(Optional.empty());
        when(bookmarkRepository.findByUserIdAndFestivalPostId(userId, post2.getId()))
                .thenReturn(Optional.empty());

        // When
        List<EventResponseDTO> result = festivalPostService.getRandomFestivalPosts(randomSize, userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(dto -> assertFalse(dto.getBookmarked()));
    }

    @Test
    @DisplayName("getRandomFestivalPosts - 북마크 등록 경우")
    void testGetRandomFestivalPosts_Bookmarked() {
        // Given
        int randomSize = 1;
        Long userId = 200L;
        FestivalPost post = createDummyFestivalPost(20L);

        when(festivalPostRepository.findRandomFestivalPosts(randomSize))
                .thenReturn(Collections.singletonList(post));
        Bookmark bookmark = createDummyBookmark(userId, post);
        when(bookmarkRepository.findByUserIdAndFestivalPostId(userId, post.getId()))
                .thenReturn(Optional.of(bookmark));

        // When
        List<EventResponseDTO> result = festivalPostService.getRandomFestivalPosts(randomSize, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getBookmarked());
    }

    @Test
    @DisplayName("getRandomFestivalPosts - 음수 randomSize 입력 시 예외 발생")
    void testGetRandomFestivalPosts_InvalidInput() {
        // Given
        int randomSize = -1;
        Long userId = 300L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> festivalPostService.getRandomFestivalPosts(randomSize, userId));
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("getFestivalPostDetail - 북마크 미등록 경우")
    void testGetFestivalPostDetail_NotBookmarked() {
        // Given
        Long id = 40L;
        Long userId = 400L;
        FestivalPost post = createDummyFestivalPost(id);
        when(festivalPostRepository.findById(id))
                .thenReturn(Optional.of(post));
        // FestivalPost 상세 조회 시 내부적으로 findByUserIdAndPerformanceId()를 호출함
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, post.getId()))
                .thenReturn(Optional.empty());

        // When
        EventResponseDTO result = festivalPostService.getFestivalPostDetail(id, userId);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertFalse(result.getBookmarked());
    }

    @Test
    @DisplayName("getFestivalPostDetail - 북마크 등록 경우")
    void testGetFestivalPostDetail_Bookmarked() {
        // Given
        Long id = 50L;
        Long userId = 500L;
        FestivalPost post = createDummyFestivalPost(id);
        when(festivalPostRepository.findById(id))
                .thenReturn(Optional.of(post));
        Bookmark bookmark = createDummyBookmark(userId, post);
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, post.getId()))
                .thenReturn(Optional.of(bookmark));

        // When
        EventResponseDTO result = festivalPostService.getFestivalPostDetail(id, userId);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertTrue(result.getBookmarked());
    }

    @Test
    @DisplayName("searchFestivalPosts - 조건에 따른 FestivalPost 검색")
    void testSearchFestivalPosts() {
        // Given
        String region = "Seoul";
        String status = "진행 예정";
        String titleKeyword = "Festival";
        int pageNum = 0;
        int pageSize = 5;
        Long userId = 600L;
        FestivalPost post = createDummyFestivalPost(60L);
        Page<FestivalPost> page = new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(pageNum, pageSize),
                1
        );
        // searchFestivalPosts() 내부에서는 bookmarkRepository.findByUserIdAndPerformanceId()를 사용함
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, post.getId()))
                .thenReturn(Optional.empty());
        when(festivalPostRepository.findAll((Specification<FestivalPost>) any(), eq(PageRequest.of(pageNum, pageSize))))
                .thenReturn(page);

        // When
        EventPageResponseDTO response = festivalPostService.searchFestivalPosts(
                region, status, titleKeyword, pageNum, pageSize, userId
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals(pageNum, response.getPageNum());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(1, response.getNumberOfElements());
        response.getPosts().forEach(dto -> assertFalse(dto.getBookmarked()));
    }
}
