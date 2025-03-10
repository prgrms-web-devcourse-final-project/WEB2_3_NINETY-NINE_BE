package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.util.RegionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FestivalPostAddressUpdateService {

    private final FestivalPostRepository festivalPostRepository;
    private final KakaoAddressService kakaoAddressService;

    public FestivalPostAddressUpdateService(FestivalPostRepository festivalPostRepository,
                                            KakaoAddressService kakaoAddressService) {
        this.festivalPostRepository = festivalPostRepository;
        this.kakaoAddressService = kakaoAddressService;
    }

    /**
     * DB에 저장된 FestivalPost의 festivalLocation 필드를 대상으로
     * 카카오 API를 호출하여 전체 주소로 업데이트하고,
     * 전체 주소를 기반으로 미리 정의한 지역 카테고리로 festivalArea를 업데이트 합니다.
     *
     * @return 업데이트된 FestivalPost 목록
     */
    @Transactional
    public List<FestivalPost> updateFestivalPostAddressesAndAreas() {
        // 1단계: kakaoAddressService를 통해 건물명을 전체 주소로 변환하여 festivalLocation 업데이트
        List<FestivalPost> posts = festivalPostRepository.findByFestivalLocationIsNotNull();
        for (FestivalPost post : posts) {
            String buildingName = post.getFestivalLocation();
            String fullAddress = kakaoAddressService.getAddressFromBuildingName(buildingName);
            if (fullAddress != null && !fullAddress.isEmpty()) {
                post.setFestivalLocation(fullAddress);
            }
        }
        festivalPostRepository.saveAll(posts);

        // 2단계: 전체 FestivalPost의 festivalLocation을 기반으로 RegionMapper를 통해 festivalArea 업데이트
        posts = festivalPostRepository.findAll();
        for (FestivalPost post : posts) {
            String festivalLocation = post.getFestivalLocation();
            if (festivalLocation != null && !festivalLocation.trim().isEmpty()) {
                String festivalArea = RegionMapper.mapRegion(festivalLocation);
                post.setFestivalArea(festivalArea);
            }
        }
        festivalPostRepository.saveAll(posts);

        return posts;
    }
}
