package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
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
     * 카카오 API를 호출하여 전체 주소로 업데이트합니다.
     *
     * @return 업데이트된 FestivalPost 목록
     */
    @Transactional
    public List<FestivalPost> updateFestivalPostAddressesAndAreas() {
        // 1단계: kakaoAddressService를 통해 건물명을 전체 주소로 변환
        List<FestivalPost> posts = festivalPostRepository.findByFestivalLocationIsNotNull();
        for (FestivalPost post : posts) {
            String buildingName = post.getFestivalLocation();
            String fullAddress = kakaoAddressService.getAddressFromBuildingName(buildingName);
            if (fullAddress != null && !fullAddress.isEmpty()) {
                post.setFestivalLocation(fullAddress);
            }
        }
        // 변경된 주소 정보를 일괄 저장
        festivalPostRepository.saveAll(posts);

        // 2단계: 전체 FestivalPost를 대상으로 festivalLocation에서 지역(앞 두 단어)을 추출하여 festivalArea 업데이트
        posts = festivalPostRepository.findAll();
        for (FestivalPost post : posts) {
            String festivalLocation = post.getFestivalLocation();
            if (festivalLocation != null && !festivalLocation.trim().isEmpty()) {
                // 공백 기준으로 문자열을 분리하여 앞의 두 단어를 추출
                String[] tokens = festivalLocation.split("\\s+");
                if (tokens.length >= 2) {
                    String festivalArea = tokens[0] + " " + tokens[1];
                    post.setFestivalArea(festivalArea);
                }
            }
        }
        // 변경된 지역 정보를 일괄 저장
        festivalPostRepository.saveAll(posts);

        return posts;
    }
}
