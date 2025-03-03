package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.PerformanceService;
import com.example.onculture.domain.user.model.Role;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PerformanceControllerTest {

    @Mock
    private PerformanceService performanceService;

    @InjectMocks
    private PerformanceController performanceController;

    private CustomUserDetails testUser;
    private EventResponseDTO sampleEvent1;

    @BeforeEach
    void setUp() {
        testUser = CustomUserDetails.builder()
                .userId(1L)
                .email("testuser@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();

        sampleEvent1 = new EventResponseDTO(
                1L,    // id
                "뮤지컬",    // genre
                "postUrl",    // postUrl
                "만 7세 이상",    // ageRating
                "뮤지컬 콘서트, 심포니 오브 뮤지컬 with 옥주현, 이지혜, 김성식",    // title
                "2025.03.29",    // startDate
                "2025.03.29",    // endDate
                "토요일(17:00)",    // operatingHours
                "서울특별시",    // location
                "강동아트센터",    // venue
                "진행중",    // status
                "인터파크 - https://www.example.com",    // ticketingWebSite
                "R석 110,000원, S석 80,000원, A석 60,000원",    // price
                "detailImageUrl",    // detailImage
                null,    // description
                false    // bookmarked
        );
    }

    @Test
    @DisplayName("getPerformances - 랜덤 공연 목록 조회")
    void testGetRandomPerformances() {
        // Given
        int randomSize = 9;

        when(performanceService.getRandomPerformances(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(sampleEvent1));

        // When
        ResponseEntity<SuccessResponse<java.util.List<EventResponseDTO>>> response =
                performanceController.getRandomPerformances(randomSize, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(sampleEvent1), response.getBody().getData());
    }

    @Test
    @DisplayName("searchPerformances - 조건에 따른 공연 검색")
    void testSearchPerformances() {
        // Given
        String region = "서울특별시";
        String status = "진행중";
        String titleKeyword = "뮤지컬";
        int pageNum = 0;
        int pageSize = 9;

        EventPageResponseDTO expectedPageResponse = new EventPageResponseDTO();
        expectedPageResponse.setPosts(Collections.singletonList(sampleEvent1));
        expectedPageResponse.setTotalPages(1);
        expectedPageResponse.setTotalElements(1);
        expectedPageResponse.setPageNum(pageNum);
        expectedPageResponse.setPageSize(pageSize);
        expectedPageResponse.setNumberOfElements(1);

        when(performanceService.searchPerformances(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(expectedPageResponse);

        // When
        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                performanceController.searchPerformances(region, status, titleKeyword, pageNum, pageSize, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPageResponse, response.getBody().getData());
    }

    @Test
    @DisplayName("getPerformance - 공연 ID로 공연 상세 정보 조회")
    void testGetPerformance() {
        // Given
        Long performanceId = 1L;

        when(performanceService.getPerformance(eq(performanceId), eq(testUser.getUserId())))
                .thenReturn(sampleEvent1);

        // When
        ResponseEntity<SuccessResponse<EventResponseDTO>> response =
                performanceController.searchPerformances(performanceId, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEvent1, response.getBody().getData());
    }
}
