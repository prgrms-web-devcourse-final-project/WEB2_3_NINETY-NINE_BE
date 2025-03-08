package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.FestivalPostAddressUpdateService;
import com.example.onculture.domain.event.service.FestivalService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/events/festivals")
@Tag(name = "축제 API", description = "축제를 관리하는 API")
public class FestivalController {

    private final FestivalService festivalService;
    private final FestivalPostAddressUpdateService festivalPostAddressUpdateService;

    // festival 랜덤 조회
    @Operation(summary = "공연(festival) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomFestivalPosts(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = festivalService.getRandomFestivalPosts(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }


    //상세 조회
    @Operation(summary = "공연(festival) 데이터 상세 정보 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/{festivalId}")
    public ResponseEntity<SuccessResponse<EventResponseDTO>> getFestivalPostDetail(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO responseDTO = festivalService
                .getFestivalPostDetail(festivalId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }

    //DB에 저장된 FestivalPost의 festivalLocation 필드를 업데이트 후 전체 목록을 반환
    @Operation(summary = "공연(festival) 건물 데이터 주소 변환",
            description = "kakaoAddressService를 사용해서 주소를 업데이트 합니다.")
    @GetMapping("/update-addresses")
    public ResponseEntity<List<FestivalPost>> updateAddresses() {
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();
        return ResponseEntity.ok(updatedPosts);
    }

    // 페스티벌 지역+상태 검색
    @Operation(summary = "공연(festival) 지역+상태 검색",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchFestivalPosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = festivalService
                .searchFestivalPosts(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    //Festival crawling 실행
    @Operation(summary = "Festival Crawling 실행",
            description = "Festival Crawling 실행")
    @GetMapping("/festival")
    public String crawlFestival() {
        try {
            festivalService.runCrawling();
            return "Festival Crawling 완료";
        } catch (Exception e) {
            return "Festival Crawling 오류: " + e.getMessage();
        }
    }
}
