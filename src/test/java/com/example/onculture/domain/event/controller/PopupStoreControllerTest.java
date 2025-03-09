package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.PopupStoreService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PopupStoreControllerTest {
    @Mock
    private PopupStoreService popupStoreService;

    @InjectMocks
    private PopupStoreController popupStoreController;

    private CustomUserDetails testUser;
    private EventResponseDTO samplePopupEvent;
    private EventPageResponseDTO sampleEventPage;
    private List<?> emptyPopupList;

    @BeforeEach
    void setUp() {
        testUser = CustomUserDetails.builder()
                .userId(1L)
                .email("testuser@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        // 예제용 팝업스토어 상세 응답 DTO
        samplePopupEvent = new EventResponseDTO(
                101L,                      // id
                "팝업스토어",                   // genre
                "http://example.com/popup.jpg", // postUrl
                null,                     // ageRating
                "팝업스토어 제목",         // title
                java.sql.Date.valueOf("2025-01-01"),               // startDate
                java.sql.Date.valueOf("2025-01-02"),               // endDate
                null,                     // operatingHours
                "서울",                  // location
                "팝업 장소",             // venue
                "진행중",                // status
                null,                     // ticketingWebSite
                null,                     // price
                null,                     // detailImage
                "팝업 상세 설명",         // description
                false                     // bookmarked
        );

        sampleEventPage = new EventPageResponseDTO();
        sampleEventPage.setPosts(Collections.singletonList(samplePopupEvent));
        sampleEventPage.setPageNum(0);
        sampleEventPage.setPageSize(9);
        sampleEventPage.setTotalElements(1);
        sampleEventPage.setTotalPages(1);
        sampleEventPage.setNumberOfElements(1);

        emptyPopupList = Collections.emptyList();
    }
    // ===== PopupStore 관련 엔드포인트 테스트 =====

    @Test
    @DisplayName("getRandomPopupStorePosts - 랜덤 팝업스토어 조회")
    void testGetRandomPopupStorePosts() {
        int randomSize = 9;
        when(popupStoreService.getRandomPopupStorePosts(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(samplePopupEvent));

        ResponseEntity<SuccessResponse<List<EventResponseDTO>>> response =
                popupStoreController.getRandomPopupStorePosts(randomSize, testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(samplePopupEvent), response.getBody().getData());
    }

    @Test
    @DisplayName("getPopupStorePostDetail - 팝업스토어 상세 조회")
    void testGetPopupStorePostDetail() {
        Long id = 101L;
        when(popupStoreService.getPopupStorePostDetail(eq(id), eq(testUser.getUserId())))
                .thenReturn(samplePopupEvent);

        ResponseEntity<EventResponseDTO> response = popupStoreController.getPopupStorePostDetail(id, testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(samplePopupEvent, response.getBody());
    }

    @Test
    @DisplayName("searchPopupStorePosts - 팝업스토어 지역+상태 검색")
    void testSearchPopupStorePostsWithFilters() {
        String region = "서울";
        String status = "진행중";
        String titleKeyword = "팝업";
        int pageNum = 0;
        int pageSize = 9;

        when(popupStoreService.searchPopupStorePosts(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(sampleEventPage);

        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                popupStoreController.searchPopupStorePosts(region, status, titleKeyword, pageNum, pageSize, testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEventPage, response.getBody().getData());
    }
    @Test
    @DisplayName("crawlPopUpStore - 성공: 정상 실행 시 'PopUpStore Crawling 완료' 반환")
    void testCrawlPopUpStoreSuccess() {
        // arrange: popupStorePostService.runCrawling()가 예외 없이 정상 실행되도록 설정
        doNothing().when(popupStoreService).runCrawling();

        // act
        String result = popupStoreController.crawlPopUpStore();

        // assert
        assertEquals("PopUpStore Crawling 완료", result);
    }

    @Test
    @DisplayName("crawlPopUpStore - 실패: 예외 발생 시 오류 메시지 반환")
    void testCrawlPopUpStoreFailure() {
        // arrange: popupStorePostService.runCrawling() 호출 시 예외 발생하도록 설정
        String errorMsg = "Test Exception";
        doThrow(new RuntimeException(errorMsg)).when(popupStoreService).runCrawling();

        // act
        String result = popupStoreController.crawlPopUpStore();

        // assert
        assertEquals("PopUpStore Crawling 오류: " + errorMsg, result);
    }
}
