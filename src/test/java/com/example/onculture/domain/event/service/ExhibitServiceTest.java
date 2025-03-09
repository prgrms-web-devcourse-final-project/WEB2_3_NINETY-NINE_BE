package com.example.onculture.domain.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.dto.PublicDataRequestDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.ExhibitRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ExhibitServiceTest {

    @Mock
    private ExhibitRepository exhibitRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private ExhibitService exhibitService;

    // 더미 데이터 생성 메서드들
    private ExhibitEntity createDummyExhibitEntity(Long seq) {
        ExhibitEntity exhibit = new ExhibitEntity();
        exhibit.setSeq(seq);
        exhibit.setTitle("전시 제목 " + seq);
        // 테스트 안정성을 위해 미래 날짜 사용 (현재보다 미래이므로 "진행 예정" 상태 예상)
        exhibit.setStartDate(java.sql.Date.valueOf("2099-01-01"));
        exhibit.setEndDate(java.sql.Date.valueOf("2099-12-31"));
        exhibit.setPlace("전시 장소");
        exhibit.setRealmName("예술");
        exhibit.setArea("서울특별시");
        exhibit.setThumbnail("thumbnail.jpg");
        exhibit.setGpsX(37.5665);
        exhibit.setGpsY(126.9780);
        exhibit.setExhibitStatus("상태 미정"); // savePublicData 호출 시 변경됨
        return exhibit;
    }

    private PublicDataRequestDTO createDummyPublicDataRequestDTO() {
        return PublicDataRequestDTO.builder()
                .title("전시 제목")
                .startDate("20990101")
                .endDate("20991231")
                .place("전시 장소")
                .realmName("예술")
                .area("서울특별시")
                .thumbnail("thumbnail.jpg")
                .gpsX(37.5665)
                .gpsY(126.9780)
                .build();
    }

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

    private Bookmark createDummyBookmark(Long userId, ExhibitEntity exhibit) {
        // 먼저, userId를 이용하여 User 객체를 생성해야 합니다.
        User user = createDummyUser(userId);
        return Bookmark.builder()
                .user(user)              // userId() 대신 user() 메소드 사용
                .exhibitEntity(exhibit)
                .build();
    }

    @Test
    @DisplayName("getExhibitDetail - 북마크 미등록 경우")
    void testGetExhibitDetail_NotBookmarked() {
        // Given
        Long seq = 10L;
        Long userId = 100L;
        ExhibitEntity exhibit = createDummyExhibitEntity(seq);

        when(exhibitRepository.findById(seq)).thenReturn(Optional.of(exhibit));
        // 상세 조회에서는 bookmarkRepository.findByUserIdAndPerformanceId 사용
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, seq))
                .thenReturn(Optional.empty());

        // When
        EventResponseDTO result = exhibitService.getExhibitDetail(seq, userId);

        // Then
        assertNotNull(result);
        assertEquals(seq, result.getId());
        assertFalse(result.getBookmarked());
    }

    @Test
    @DisplayName("getExhibitDetail - 북마크 등록 경우")
    void testGetExhibitDetail_Bookmarked() {
        // Given
        Long seq = 20L;
        Long userId = 200L;
        ExhibitEntity exhibit = createDummyExhibitEntity(seq);

        when(exhibitRepository.findById(seq)).thenReturn(Optional.of(exhibit));
        // 북마크가 존재하는 경우
        Bookmark bookmark = createDummyBookmark(userId, exhibit);
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, seq))
                .thenReturn(Optional.of(bookmark));

        // When
        EventResponseDTO result = exhibitService.getExhibitDetail(seq, userId);

        // Then
        assertNotNull(result);
        assertEquals(seq, result.getId());
        assertTrue(result.getBookmarked());
    }

    @Test
    @DisplayName("savePublicData - 공공데이터 저장")
    void testSavePublicData() {
        // Given
        PublicDataRequestDTO requestDTO = createDummyPublicDataRequestDTO();

        // When
        exhibitService.savePublicData(requestDTO);

        // Then
        // 저장된 ExhibitEntity를 캡처하여 필드 값 확인
        ArgumentCaptor<ExhibitEntity> captor = ArgumentCaptor.forClass(ExhibitEntity.class);
        verify(exhibitRepository, times(1)).save(captor.capture());
        ExhibitEntity savedEntity = captor.getValue();

        assertEquals(requestDTO.getTitle(), savedEntity.getTitle());
        // 미래 날짜이므로 today.before(startDate) 조건에 의해 "진행 예정"이 설정되어야 함
        assertEquals("진행 예정", savedEntity.getExhibitStatus());
    }

    @Test
    @DisplayName("getRandomExhibitions - 북마크 미등록 경우")
    void testGetRandomExhibitions_NotBookmarked() {
        // Given
        int randomSize = 2;
        Long userId = 300L;
        ExhibitEntity exhibit1 = createDummyExhibitEntity(40L);
        ExhibitEntity exhibit2 = createDummyExhibitEntity(41L);

        when(exhibitRepository.findRandomExhibitions(randomSize))
                .thenReturn(Arrays.asList(exhibit1, exhibit2));
        // 랜덤 조회에서는 bookmarkRepository.findByUserIdAndExhibitEntitySeq 사용
        when(bookmarkRepository.findByUserIdAndExhibitEntitySeq(userId, exhibit1.getSeq()))
                .thenReturn(Optional.empty());
        when(bookmarkRepository.findByUserIdAndExhibitEntitySeq(userId, exhibit2.getSeq()))
                .thenReturn(Optional.empty());

        // When
        List<EventResponseDTO> result = exhibitService.getRandomExhibitions(randomSize, userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(dto -> assertFalse(dto.getBookmarked()));
    }

    @Test
    @DisplayName("getRandomExhibitions - 북마크 등록 경우")
    void testGetRandomExhibitions_Bookmarked() {
        // Given
        int randomSize = 1;
        Long userId = 400L;
        ExhibitEntity exhibit = createDummyExhibitEntity(50L);

        when(exhibitRepository.findRandomExhibitions(randomSize))
                .thenReturn(Collections.singletonList(exhibit));
        Bookmark bookmark = createDummyBookmark(userId, exhibit);
        when(bookmarkRepository.findByUserIdAndExhibitEntitySeq(userId, exhibit.getSeq()))
                .thenReturn(Optional.of(bookmark));

        // When
        List<EventResponseDTO> result = exhibitService.getRandomExhibitions(randomSize, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getBookmarked());
    }

    @Test
    @DisplayName("getRandomExhibitions - 음수 randomSize 입력 시 예외 발생")
    void testGetRandomExhibitions_InvalidInput() {
        // Given
        int randomSize = -1;
        Long userId = 500L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> exhibitService.getRandomExhibitions(randomSize, userId));
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("searchExhibits - 조건에 따른 전시 검색")
    void testSearchExhibits() {
        // Given
        String region = "서울특별시";
        String status = "진행 예정";
        String titleKeyword = "전시";
        int pageNum = 0;
        int pageSize = 5;
        Long userId = 600L;
        ExhibitEntity exhibit = createDummyExhibitEntity(60L);

        Page<ExhibitEntity> page = new PageImpl<>(
                Collections.singletonList(exhibit),
                PageRequest.of(pageNum, pageSize),
                1
        );

        // 상세 검색에서는 bookmarkRepository.findByUserIdAndPerformanceId 사용
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, exhibit.getSeq()))
                .thenReturn(Optional.empty());
        when(exhibitRepository.findAll((Specification<ExhibitEntity>) any(), eq(PageRequest.of(pageNum, pageSize))))
                .thenReturn(page);

        // When
        EventPageResponseDTO response = exhibitService.searchExhibits(region, status, titleKeyword, pageNum, pageSize, userId);

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
