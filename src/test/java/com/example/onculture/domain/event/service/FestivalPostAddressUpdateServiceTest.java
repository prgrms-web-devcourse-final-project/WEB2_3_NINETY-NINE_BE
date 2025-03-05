package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

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
    void testUpdateFestivalPostAddressesAndAreas_Success() {
        // 1단계: festivalPostRepository.findByFestivalLocationIsNotNull()가 samplePost를 반환하도록 설정
        List<FestivalPost> initialPosts = new ArrayList<>(Arrays.asList(samplePost));
        when(festivalPostRepository.findByFestivalLocationIsNotNull()).thenReturn(initialPosts);
        // KakaoAddressService가 "코엑스"에 대해 전체 주소 "서울 강남구 코엑스몰"을 반환하도록 설정
        when(kakaoAddressService.getAddressFromBuildingName("코엑스")).thenReturn("서울 강남구 코엑스몰");

        // 두 번째 단계에서 findAll() 호출 시, 이미 변경된 samplePost를 반환하도록 설정
        when(festivalPostRepository.findAll()).thenReturn(initialPosts);
        // saveAll()은 변경된 리스트를 그대로 반환하는 것으로 가정

        // 실행
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();

        // 검증:
        // 첫 번째 단계에서 festivalLocation이 "서울 강남구 코엑스몰"로 업데이트되어야 함.
        // 두 번째 단계에서 festivalLocation을 공백 기준으로 분리하여 festivalArea가 "서울 강남구"로 설정되어야 함.
        assertNotNull(updatedPosts);
        assertFalse(updatedPosts.isEmpty());

        FestivalPost updatedPost = updatedPosts.get(0);
        assertEquals("서울 강남구 코엑스몰", updatedPost.getFestivalLocation());
        assertEquals("서울 강남구", updatedPost.getFestivalArea());
    }

    @Test
    void testUpdateFestivalPostAddressesAndAreas_NoAddressFound() {
        // 경우: KakaoAddressService가 빈 문자열이나 null을 반환하면 기존 festivalLocation은 그대로 유지되고, festivalArea 업데이트는 건너뛰게 됨.

        List<FestivalPost> initialPosts = new ArrayList<>(Arrays.asList(samplePost));
        when(festivalPostRepository.findByFestivalLocationIsNotNull()).thenReturn(initialPosts);
        // 빈 문자열 반환: 즉, 전체 주소를 얻지 못한 경우.
        when(kakaoAddressService.getAddressFromBuildingName("코엑스")).thenReturn("");

        when(festivalPostRepository.findAll()).thenReturn(initialPosts);

        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();

        FestivalPost updatedPost = updatedPosts.get(0);
        // festivalLocation은 변경되지 않고 "코엑스" 그대로, festivalArea는 업데이트되지 않음.
        assertEquals("코엑스", updatedPost.getFestivalLocation());
        assertNull(updatedPost.getFestivalArea());
    }
}
