package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.util.RegionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FestivalPostAddressUpdateServiceTest {

    @Mock
    private FestivalPostRepository festivalPostRepository;

    @Mock
    private KakaoAddressService kakaoAddressService;

    @InjectMocks
    private FestivalPostAddressUpdateService festivalPostAddressUpdateService;

    private FestivalPost samplePost;

    @BeforeEach
    void setUp() {
        // 초기 상태: festivalLocation은 건물명("코엑스")로 저장되어 있음.
        samplePost = new FestivalPost();
        samplePost.setId(1L);
        samplePost.setFestivalLocation("코엑스");
        // festivalArea는 아직 설정되지 않음.
        samplePost.setFestivalArea(null);
    }

    @Test
    @DisplayName("성공 케이스: 전체 주소 및 지역 업데이트")
    void testUpdateFestivalPostAddressesAndAreas_Success() {
        // Given:
        // - 초기 게시글의 festivalLocation은 "코엑스"
        // - KakaoAddressService는 "코엑스"에 대해 전체 주소 "서울 강남구 코엑스몰"을 반환
        // - RegionMapper.mapRegion("서울 강남구 코엑스몰")는 "서울특별시"를 반환 (주소에 "서울" 포함)
        List<FestivalPost> initialPosts = new ArrayList<>(Arrays.asList(samplePost));
        when(festivalPostRepository.findByFestivalLocationIsNotNull()).thenReturn(initialPosts);
        when(kakaoAddressService.getAddressFromBuildingName("코엑스")).thenReturn("서울 강남구 코엑스몰");
        // 첫 번째 단계 이후, 두 번째 단계에서 findAll()로 업데이트된 게시글을 반환하도록 설정
        when(festivalPostRepository.findAll()).thenReturn(initialPosts);

        // When: 서비스 메서드 실행
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();

        // Then:
        // - festivalLocation은 "서울 강남구 코엑스몰"로 업데이트되고,
        // - RegionMapper에 의해 festivalArea는 "서울특별시"로 설정됨.
        FestivalPost updatedPost = updatedPosts.get(0);
        assertEquals("서울 강남구 코엑스몰", updatedPost.getFestivalLocation());
        assertEquals("서울특별시", updatedPost.getFestivalArea());

        verify(festivalPostRepository).findByFestivalLocationIsNotNull();
        verify(kakaoAddressService).getAddressFromBuildingName("코엑스");
        // saveAll()이 두 단계 모두에서 호출됨
        verify(festivalPostRepository, times(2)).saveAll(anyList());
        verify(festivalPostRepository).findAll();
    }

    @Test
    @DisplayName("실패 케이스: 빈 문자열 반환 시 주소 업데이트 건너뛰고 지역은 '해외'로 매핑")
    void testUpdateFestivalPostAddressesAndAreas_NoAddressFound_Empty() {
        // Given:
        // - KakaoAddressService가 빈 문자열("")을 반환하는 경우,
        //   따라서 festivalLocation은 업데이트되지 않고 "코엑스" 그대로 유지됨.
        // - 두 번째 단계에서 RegionMapper.mapRegion("코엑스") 호출 결과 "코엑스"에 매칭되는 키워드가 없으므로 "해외" 반환.
        List<FestivalPost> initialPosts = new ArrayList<>(Arrays.asList(samplePost));
        when(festivalPostRepository.findByFestivalLocationIsNotNull()).thenReturn(initialPosts);
        when(kakaoAddressService.getAddressFromBuildingName("코엑스")).thenReturn("");
        when(festivalPostRepository.findAll()).thenReturn(initialPosts);

        // When:
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();

        // Then:
        FestivalPost updatedPost = updatedPosts.get(0);
        assertEquals("코엑스", updatedPost.getFestivalLocation());
        assertEquals("해외", updatedPost.getFestivalArea());

        verify(festivalPostRepository).findByFestivalLocationIsNotNull();
        verify(kakaoAddressService).getAddressFromBuildingName("코엑스");
        verify(festivalPostRepository, times(2)).saveAll(anyList());
        verify(festivalPostRepository).findAll();
    }

    @Test
    @DisplayName("실패 케이스: null 반환 시 주소 업데이트 건너뛰고 지역은 '해외'로 매핑")
    void testUpdateFestivalPostAddressesAndAreas_NoAddressFound_Null() {
        // Given:
        // - KakaoAddressService가 null을 반환하는 경우,
        //   따라서 festivalLocation은 업데이트되지 않고 "코엑스" 그대로 유지됨.
        // - 두 번째 단계에서 RegionMapper.mapRegion("코엑스") 호출 결과 "해외" 반환.
        List<FestivalPost> initialPosts = new ArrayList<>(Arrays.asList(samplePost));
        when(festivalPostRepository.findByFestivalLocationIsNotNull()).thenReturn(initialPosts);
        when(kakaoAddressService.getAddressFromBuildingName("코엑스")).thenReturn(null);
        when(festivalPostRepository.findAll()).thenReturn(initialPosts);

        // When:
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();

        // Then:
        FestivalPost updatedPost = updatedPosts.get(0);
        assertEquals("코엑스", updatedPost.getFestivalLocation());
        assertEquals("해외", updatedPost.getFestivalArea());

        verify(festivalPostRepository).findByFestivalLocationIsNotNull();
        verify(kakaoAddressService).getAddressFromBuildingName("코엑스");
        verify(festivalPostRepository, times(2)).saveAll(anyList());
        verify(festivalPostRepository).findAll();
    }
}
