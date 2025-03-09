package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PerformanceServiceTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private PerformanceService performanceService;

    // Dummy 데이터 생성 메서드들
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

    private Performance createDummyPerformance(Long id) {
        Performance performance = new Performance();
        performance.setId(id);
        performance.setPerformanceId("performanceId");
        performance.setFacilityId("facilityId");
        performance.setPerformanceTitle("공연 제목");
        performance.setStartDate(java.sql.Date.valueOf("2025-01-01"));
        performance.setEndDate(java.sql.Date.valueOf("2025-01-31"));
        performance.setFacilityName("코엑스 아티움");
        performance.setRuntime("120분");
        performance.setAgeRating("전체 관람가");
        performance.setTicketPrice("전석 45000원");
        performance.setPosterUrl("post.jpg");
        performance.setArea("서울특별시");
        performance.setGenre("뮤지컬");
        performance.setUpdateDate("2025-01-01");
        performance.setPerformanceState("진행예정");
        performance.setIntroduction("공연 소개 " + id);
        performance.setShowTimes("매일 19:00");
        performance.setStyleUrls("detailImage.jpg");
        performance.setRelatedLinks("relatedUrl");
        return performance;
    }

    private Bookmark createDummyBookmark(Long userId, Performance performance) {
        User user = createDummyUser(userId);
        return Bookmark.builder()
                .user(user)
                .performance(performance)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 테스트에서 반복 사용될 변수들 (필요에 따라 개별 테스트 메서드에서도 생성 가능)
    private User dummyUser;
    private Performance dummyPerformance;

    @BeforeEach
    void setUp() {
        // 셋업 단계에서 공통 dummy 데이터 생성
        dummyUser = createDummyUser(1L);
        dummyPerformance = createDummyPerformance(100L);
    }

    @Test
    @DisplayName("getRandomPerformances - 북마크 미등록 경우")
    void testGetRandomPerformances_NotBookmarked() {
        // Given
        int randomSize = 2;
        Long userId = dummyUser.getId();
        Performance performance1 = createDummyPerformance(101L);
        Performance performance2 = createDummyPerformance(102L);

        when(performanceRepository.findRandomPerformances(eq(randomSize)))
                .thenReturn(Arrays.asList(performance1, performance2));

        // 북마크 미등록
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performance1.getId())))
                .thenReturn(Optional.empty());
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performance2.getId())))
                .thenReturn(Optional.empty());

        // When
        List<EventResponseDTO> results = performanceService.getRandomPerformances(randomSize, userId);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        results.forEach(dto -> assertFalse(dto.getBookmarked()));
    }

    @Test
    @DisplayName("getRandomPerformances - 북마크 등록 경우")
    void testGetRandomPerformances_Bookmarked() {
        // Given
        int randomSize = 1;
        Long userId = dummyUser.getId();
        Performance performance = createDummyPerformance(201L);

        when(performanceRepository.findRandomPerformances(eq(randomSize)))
                .thenReturn(Collections.singletonList(performance));

        // 북마크 등록
        Bookmark bookmark = createDummyBookmark(userId, performance);
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performance.getId())))
                .thenReturn(Optional.of(bookmark));

        // When
        List<EventResponseDTO> results = performanceService.getRandomPerformances(randomSize, userId);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getBookmarked());
    }

    @Test
    @DisplayName("getRandomPerformances - 음수 randomSize 입력 시 예외 발생")
    void testGetRandomPerformances_InvalidInput() {
        // Given
        int randomSize = -1;
        Long userId = dummyUser.getId();

        // When & Then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> performanceService.getRandomPerformances(randomSize, userId)
        );
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("searchPerformances - 조건에 따른 공연 검색 결과")
    void testSearchPerformances() {
        // Given
        String region = "서울특별시";
        String status = "진행예정";
        String titleKeyword = "뮤지컬";
        int pageNum = 0;
        int pageSize = 9;
        Long userId = dummyUser.getId();

        Performance performance = createDummyPerformance(301L);
        Page<Performance> performancePage = new PageImpl<>(
                Collections.singletonList(performance),
                PageRequest.of(pageNum, pageSize),
                1
        );

        // 북마크 미등록
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performance.getId())))
                .thenReturn(Optional.empty());

        // 스펙 모호성 해결: (Specification<Performance>) any()
        when(performanceRepository.findAll((Specification<Performance>) any(), eq(PageRequest.of(pageNum, pageSize))))
                .thenReturn(performancePage);

        // When
        EventPageResponseDTO response = performanceService.searchPerformances(
                region, status, titleKeyword, pageNum, pageSize, userId
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals(pageNum, response.getPageNum());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(1, response.getNumberOfElements());
        // 북마크 미등록이므로 false
        assertFalse(response.getPosts().get(0).getBookmarked());
    }

    @Test
    @DisplayName("getPerformance - 공연 ID로 상세 조회, 북마크 미등록")
    void testGetPerformance_NotBookmarked() {
        // Given
        Long performanceId = 401L;
        Long userId = dummyUser.getId();
        Performance performance = createDummyPerformance(performanceId);

        when(performanceRepository.findById(eq(performanceId)))
                .thenReturn(Optional.of(performance));
        // 북마크 미등록
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performanceId)))
                .thenReturn(Optional.empty());

        // When
        EventResponseDTO result = performanceService.getPerformance(performanceId, userId);

        // Then
        assertNotNull(result);
        assertEquals(performanceId, result.getId());
        assertFalse(result.getBookmarked());
    }

    @Test
    @DisplayName("getPerformance - 공연 ID로 상세 조회, 북마크 등록")
    void testGetPerformance_Bookmarked() {
        // Given
        Long performanceId = 501L;
        Long userId = dummyUser.getId();
        Performance performance = createDummyPerformance(performanceId);

        when(performanceRepository.findById(eq(performanceId)))
                .thenReturn(Optional.of(performance));
        // 북마크 등록
        Bookmark bookmark = createDummyBookmark(userId, performance);
        when(bookmarkRepository.findByUserIdAndPerformanceId(eq(userId), eq(performanceId)))
                .thenReturn(Optional.of(bookmark));

        // When
        EventResponseDTO result = performanceService.getPerformance(performanceId, userId);

        // Then
        assertNotNull(result);
        assertEquals(performanceId, result.getId());
        assertTrue(result.getBookmarked());
    }
}
