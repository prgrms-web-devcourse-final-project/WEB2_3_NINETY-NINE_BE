package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.FestivalPostAddressUpdateService;
import com.example.onculture.domain.event.service.FestivalService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.domain.user.model.Role;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FestivalControllerTest {
    @Mock
    private FestivalService festivalService;

    @Mock
    private FestivalPostAddressUpdateService festivalPostAddressUpdateService;

    @InjectMocks
    private FestivalController festivalController;

    private CustomUserDetails testUser;
    private EventResponseDTO sampleFestivalEvent;
    private EventPageResponseDTO sampleEventPage;
    private List<FestivalPost> emptyFestivalList;

    @BeforeEach
    void setUp() {
        testUser = CustomUserDetails.builder()
                .userId(1L)
                .email("testuser@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        // 예제용 페스티벌 상세 응답 DTO
        sampleFestivalEvent = new EventResponseDTO(
                202L,                      // id
                null,                     // genre
                "http://example.com/festival.jpg", // postUrl
                null,                     // ageRating
                "페스티벌 제목",           // title
                "20250201",               // startDate
                "20250205",               // endDate
                null,                     // operatingHours
                "서울",                   // location
                "페스티벌 장소",          // venue
                "진행중",                 // status
                null,                     // ticketingWebSite
                null,                     // price
                null,                     // detailImage
                "페스티벌 상세 설명",      // description
                false                     // bookmarked
        );

        // 예제용 페이지네이션 응답 DTO (단일 항목)
        sampleEventPage = new EventPageResponseDTO();
        sampleEventPage.setPosts(Collections.singletonList(sampleFestivalEvent));
        sampleEventPage.setPageNum(0);
        sampleEventPage.setPageSize(9);
        sampleEventPage.setTotalElements(1);
        sampleEventPage.setTotalPages(1);
        sampleEventPage.setNumberOfElements(1);

        emptyFestivalList = Collections.emptyList();
    }

    // ===== Festival 관련 엔드포인트 테스트 =====

    @Test
    @DisplayName("getRandomFestivalPosts - 랜덤 페스티벌 조회")
    void testGetRandomFestivalPosts() {
        int randomSize = 9;

        when(festivalService.getRandomFestivalPosts(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(sampleFestivalEvent));

        // When
        ResponseEntity<SuccessResponse<List<EventResponseDTO>>> response =
                festivalController.getRandomFestivalPosts(randomSize, testUser);
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // SuccessResponse 내부의 data를 비교하도록 변경
        assertEquals(Collections.singletonList(sampleFestivalEvent), response.getBody().getData());
    }

    @Test
    @DisplayName("getFestivalPostDetail - 페스티벌 상세 조회")
    void testGetFestivalPostDetail() {
        // Given
        Long id = 202L;

        when(festivalService.getFestivalPostDetail(eq(id), eq(testUser.getUserId())))
                .thenReturn(sampleFestivalEvent);

        // When
        ResponseEntity<SuccessResponse<EventResponseDTO>> response =
                festivalController.getFestivalPostDetail(id, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // SuccessResponse 내부의 data를 비교하도록 변경
        assertEquals(sampleFestivalEvent, response.getBody().getData());
    }

    @Test
    @DisplayName("updateAddresses - 페스티벌 주소 업데이트")
    void testUpdateAddresses() {
        List<FestivalPost> emptyList = Collections.emptyList();
        when(festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas())
                .thenReturn(emptyList);
        // When
        ResponseEntity<List<FestivalPost>> response = festivalController.updateAddresses();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyList, response.getBody());
    }

    @Test
    @DisplayName("searchFestivalPosts - 페스티벌 지역+상태 검색")
    void testSearchFestivalPostsWithFilters() {
        // Given
        String region = "서울";
        String status = "진행중";
        String titleKeyword = "페스티벌";
        int pageNum = 0;
        int pageSize = 9;

        when(festivalService.searchFestivalPosts(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(sampleEventPage);
        // When
        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                festivalController.searchFestivalPosts(region, status, titleKeyword, pageNum, pageSize, testUser);
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // SuccessResponse 내부의 data를 비교하도록 변경
        assertEquals(sampleEventPage, response.getBody().getData());
    }

    @Test
    @DisplayName("crawlFestival - 성공: 정상 실행 시 'Festival Crawling 완료' 반환")
    void testCrawlFestivalSuccess() {
        // arrange: festivalPostService.runCrawling()가 예외 없이 실행되도록 설정
        doNothing().when(festivalService).runCrawling();

        // act
        String result = festivalController.crawlFestival();

        // assert
        assertEquals("Festival Crawling 완료", result);
    }

    @Test
    @DisplayName("crawlFestival - 실패: 예외 발생 시 오류 메시지 반환")
    void testCrawlFestivalFailure() {
        // arrange: festivalPostService.runCrawling() 호출 시 예외 발생하도록 설정
        String errorMsg = "Festival Error";
        doThrow(new RuntimeException(errorMsg)).when(festivalService).runCrawling();

        // act
        String result = festivalController.crawlFestival();

        // assert
        assertEquals("Festival Crawling 오류: " + errorMsg, result);
    }
}
