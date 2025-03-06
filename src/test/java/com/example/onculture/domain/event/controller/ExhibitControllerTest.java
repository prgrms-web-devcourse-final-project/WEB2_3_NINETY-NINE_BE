package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.service.ExhibitService;
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
public class ExhibitControllerTest {

    @Mock
    private ExhibitService exhibitService;

    @InjectMocks
    private ExhibitController exhibitController;

    private CustomUserDetails testUser;
    private EventResponseDTO sampleExhibitDetail;
    private EventPageResponseDTO samplePageResponse;

    @BeforeEach
    void setUp() {
        testUser = CustomUserDetails.builder()
                .userId(1L)
                .email("testuser@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        // 전시 상세 조회 시 반환할 예제 DTO (북마크 여부 포함)
        sampleExhibitDetail = new EventResponseDTO(
                10L,                      // id
                "전시",                   // genre (여기서는 area를 전시 장르로 사용)
                "http://example.com/thumbnail.jpg", // postUrl
                null,                     // ageRating
                "전시 제목",              // title
                "20250101",               // startDate
                "20250131",               // endDate
                null,                     // operatingHours
                "서울",                  // location
                "서울전시장",            // venue
                "진행중",                // status
                null,                     // ticketingWebSite
                null,                     // price
                null,                     // detailImage
                "전시 상세 설명",         // description
                false                     // bookmarked
        );

        // 검색 시 반환할 페이지네이션 응답 DTO (단일 항목)
        samplePageResponse = new EventPageResponseDTO();
        samplePageResponse.setPosts(Collections.singletonList(sampleExhibitDetail));
        samplePageResponse.setPageNum(0);
        samplePageResponse.setPageSize(9);
        samplePageResponse.setTotalElements(1);
        samplePageResponse.setTotalPages(1);
        samplePageResponse.setNumberOfElements(1);
    }

    @Test
    @DisplayName("getRandomExhibitions - 랜덤 전시 목록 조회")
    void testGetRandomExhibitions() {
        int randomSize = 9;

        when(exhibitService.getRandomExhibitions(eq(randomSize), eq(testUser.getUserId())))
                .thenReturn(Collections.singletonList(sampleExhibitDetail));

        ResponseEntity<SuccessResponse<List<EventResponseDTO>>> response =
                exhibitController.getRandomExhibitions(randomSize, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(sampleExhibitDetail), response.getBody().getData());
    }

    @Test
    @DisplayName("searchExhibits - 조건에 따른 전시 검색")
    void testSearchExhibits() {
        String region = "서울";
        String status = "진행중";
        String titleKeyword = "전시";
        int pageNum = 0;
        int pageSize = 9;

        when(exhibitService.searchExhibits(eq(region), eq(status), eq(titleKeyword),
                eq(pageNum), eq(pageSize), eq(testUser.getUserId())))
                .thenReturn(samplePageResponse);

        ResponseEntity<SuccessResponse<EventPageResponseDTO>> response =
                exhibitController.searchExhibits(region, status, titleKeyword, pageNum, pageSize, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(samplePageResponse, response.getBody().getData());
    }

    @Test
    @DisplayName("getExhibitionDetail - 전시 ID로 전시 상세 정보 조회")
    void testGetExhibitionDetail() {
        Long seq = 10L;

        when(exhibitService.getExhibitDetail(eq(seq), eq(testUser.getUserId())))
                .thenReturn(sampleExhibitDetail);

        ResponseEntity<EventResponseDTO> response =
                exhibitController.getExhibitionDetail(seq, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleExhibitDetail, response.getBody());
    }
}
