package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.service.FestivalPostAddressUpdateService;
import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {

    @Mock
    private PopupStorePostService popupStorePostService;

    @Mock
    private FestivalPostService festivalPostService;

    @Mock
    private FestivalPostAddressUpdateService festivalPostAddressUpdateService;

    @InjectMocks
    private EventController eventController;

    private CustomUserDetails testUser;
    private EventResponseDTO samplePopupEvent;
    private EventResponseDTO sampleFestivalEvent;
    private EventPageResponseDTO sampleEventPage;
    private List<FestivalPost> emptyFestivalList;
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
                "팝업",                   // genre
                "http://example.com/popup.jpg", // postUrl
                null,                     // ageRating
                "팝업스토어 제목",         // title
                "20250101",               // startDate
                "20250102",               // endDate
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
                "서울",                  // location
                "페스티벌 장소",         // venue
                "진행중",                // status
                null,                     // ticketingWebSite
                null,                     // price
                null,                     // detailImage
                "페스티벌 상세 설명",     // description
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
        emptyPopupList = Collections.emptyList();
    }

    // ===== PopupStore 관련 엔드포인트 테스트 =====

    @Test
    @DisplayName("listAllPopupStorePosts - 전체 팝업스토어 목록 조회")
    void testListAllPopupStorePosts() {
        when(popupStorePostService.listAll()).thenReturn((List<PopupStorePost>) emptyPopupList);
        List<?> result = eventController.listAllPopupStorePosts();
        assertEquals(emptyPopupList, result);
    }

    @Test
    @DisplayName("searchPopupStorePosts - 제목 검색 팝업스토어 조회")
    void testSearchPopupStorePosts() {
        String title = "팝업";
        when(popupStorePostService.searchByTitle(eq(title))).thenReturn((List<PopupStorePost>) emptyPopupList);
        List<?> result = eventController.searchPopupStorePosts(title);
        assertEquals(emptyPopupList, result);
    }

    @Test
    @DisplayName("getRandomPopupStorePosts - 랜덤 팝업스토어 조회")
    void testGetRandomPopupStorePosts() {
        int randomSize = 9;
        when(popupStorePostService.getRandomPopupStorePosts(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(samplePopupEvent));

        ResponseEntity<SuccessResponse<List<EventResponseDTO>>> response =
                eventController.getRandomPopupStorePosts(randomSize, testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(samplePopupEvent), response.getBody().getData());
    }

    @Test
    @DisplayName("getPopupStorePostDetail - 팝업스토어 상세 조회")
    void testGetPopupStorePostDetail() {
        Long id = 101L;
        when(popupStorePostService.getPopupStorePostDetail(eq(id), eq(testUser.getUserId())))
                .thenReturn(samplePopupEvent);

        ResponseEntity<EventResponseDTO> response = eventController.getPopupStorePostDetail(id, testUser);
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

        when(popupStorePostService.searchPopupStorePosts(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(sampleEventPage);

        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                eventController.searchPopupStorePosts(region, status, titleKeyword, pageNum, pageSize, testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEventPage, response.getBody().getData());
    }

    // ===== Festival 관련 엔드포인트 테스트 =====

    @Test
    @DisplayName("listAllFestivalPosts - 전체 페스티벌 목록 조회")
    void testListAllFestivalPosts() {
        when(festivalPostService.listAll()).thenReturn(emptyFestivalList);

        // When
        List<?> result = eventController.listAllFestivalPosts();

        // Then
        assertEquals(emptyFestivalList, result);
    }

    @Test
    @DisplayName("searchFestivalPosts - 제목 검색 페스티벌 조회")
    void testSearchFestivalPosts() {

        // Given
        String title = "페스티벌";

        when(festivalPostService.searchByTitle(eq(title))).thenReturn(emptyFestivalList);

        // When
        List<?> result = eventController.searchFestivalPosts(title);

        // Then
        assertEquals(emptyFestivalList, result);
    }

    @Test
    @DisplayName("getRandomFestivalPosts - 랜덤 페스티벌 조회")
    void testGetRandomFestivalPosts() {
        int randomSize = 9;

        when(festivalPostService.getRandomFestivalPosts(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(sampleFestivalEvent));

        // When
        ResponseEntity<SuccessResponse<List<EventResponseDTO>>> response =
                eventController.getRandomFestivalPosts(randomSize, testUser);
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(sampleFestivalEvent), response.getBody().getData());
    }

    @Test
    @DisplayName("getFestivalPostDetail - 페스티벌 상세 조회")
    void testGetFestivalPostDetail() {
        // Given
        Long id = 202L;

        when(festivalPostService.getFestivalPostDetail(eq(id), eq(testUser.getUserId())))
                .thenReturn(sampleFestivalEvent);

        // When
        ResponseEntity<SuccessResponse<EventResponseDTO>> response =
                eventController.getFestivalPostDetail(id, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleFestivalEvent, response.getBody());
    }

    @Test
    @DisplayName("updateAddresses - 페스티벌 주소 업데이트")
    void testUpdateAddresses() {
        List<FestivalPost> emptyList = Collections.emptyList();
        when(festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas())
                .thenReturn(emptyList);
        // When
        ResponseEntity<List<FestivalPost>> response = eventController.updateAddresses();

        //Then
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

        when(festivalPostService.searchFestivalPosts(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(sampleEventPage);
        // When
        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                eventController.searchFestivalPosts(region, status, titleKeyword, pageNum, pageSize, testUser);
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleEventPage, response.getBody().getData());
    }
}
