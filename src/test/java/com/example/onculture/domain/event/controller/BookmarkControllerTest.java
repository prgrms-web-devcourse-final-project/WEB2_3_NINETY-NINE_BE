package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.BookmarkEventListDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.BookmarkService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookmarkControllerTest {

    @Mock
    private BookmarkService bookmarkService;

    @InjectMocks
    private BookmarkController bookmarkController;

    private CustomUserDetails testUser;

    private EventResponseDTO event1;
    private EventResponseDTO event2;

    @BeforeEach
    void setUp() {
        testUser = CustomUserDetails.builder()
                .userId(1L)
                .email("testuser@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();

        event1 = new EventResponseDTO(
                101L,                           // id
                "theater",                         // genre
                "http://example.com/event1",       // postUrl
                "All",                             // ageRating
                "Event One",                       // title
                "2025-03-01",                      // startDate
                "2025-03-10",                      // endDate
                "10:00-22:00",                     // operatingHours
                "Location 1",                      // location
                "Venue 1",                         // venue
                "Active",                          // status
                "http://tickets.com/event1",       // ticketingWebSite
                "$50",                             // price
                "http://images.com/event1.jpg",    // detailImage
                "Description for event one",       // description
                true                               // isBookmarked
        );

        event2 = new EventResponseDTO(
                102L,                           // id
                "concert",                         // genre
                "http://example.com/event2",       // postUrl
                "18+",                             // ageRating
                "Event Two",                       // title
                "2025-04-01",                      // startDate
                "2025-04-05",                      // endDate
                "12:00-23:00",                     // operatingHours
                "Location 2",                      // location
                "Venue 2",                         // venue
                "Inactive",                        // status
                "http://tickets.com/event2",       // ticketingWebSite
                "$80",                             // price
                "http://images.com/event2.jpg",    // detailImage
                "Description for event two",       // description
                false                              // isBookmarked
        );
    }

    @Test
    @DisplayName("toggleBookmark - 공연: 북마크가 이미 존재하면 삭제 요청, 존재하지 않으면 추가 요청")
    void testToggleBookmark() {
        // Given
        Long eventPostId = 1L;
        String genre = "theater";
        String expectedMessage = "Toggled";

        when(bookmarkService.toggleBookmark(eq(1L), eq(eventPostId), eq(genre)))
                .thenReturn(expectedMessage);

        // When
        ResponseEntity<SuccessResponse<String>> response =
                bookmarkController.toggleBookmark(eventPostId, genre, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedMessage, response.getBody().getData());
    }

    @Test
    @DisplayName("getMyBookmarkedEvents - 유저가 북마크한 공연 목록 조회")
    void testGetMyBookmarkedEvents() {
        // Given
        int page = 0;
        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);

        BookmarkEventListDTO expectedDTO = new BookmarkEventListDTO();
        expectedDTO.setPosts(Arrays.asList(event1, event2));
        expectedDTO.setTotalPages(5);
        expectedDTO.setTotalElements(2);
        expectedDTO.setPageNum(page);
        expectedDTO.setPageSize(pageSize);
        expectedDTO.setNumberOfElements(2);

        when(bookmarkService.getBookmarkedEvents(eq(1L), eq(pageable)))
                .thenReturn(expectedDTO);

        // When
        ResponseEntity<SuccessResponse<BookmarkEventListDTO>> response =
                bookmarkController.getMyBookmarkedEvents(testUser, page, pageSize);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody().getData());
    }
}
