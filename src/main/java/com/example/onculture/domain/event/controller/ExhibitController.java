package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.dto.ExhibitDetailDTO;
import com.example.onculture.domain.event.service.ExhibitService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/exhibits")
@RequiredArgsConstructor
@Tag(name = "전시회 API", description = "전시회를 관리하는 API")
public class ExhibitController {

    private final ExhibitService exhibitService;

    // 공공 데이터 API 저장
    @Operation(summary = "전시회 공공 데이터 저장",
            description = "공공데이터 OPEN API를 사용해서 데이터를 저장하는 API입니다")
    @PostMapping("/fetchXmlAndSave")
    public ResponseEntity<String> fetchXmlAndSave() {
        exhibitService.fetchXmlAndSaveWithPagination();
        return ResponseEntity.ok("공공데이터 XML을 불러와 저장했습니다.");
    }

    // 전시 상세 정보 조회
    @Operation(summary = "공연(전시회) 게시글 상세 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/{exhibitId}")
    public ResponseEntity<EventResponseDTO> getExhibitionDetail(
            @PathVariable Long exhibitId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO responseDTO = exhibitService.getExhibitDetail(exhibitId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO).getData());
    }

    //랜덤 조회
    @Operation(summary = "공연(전시회) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomExhibitions(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = exhibitService
                .getRandomExhibitions(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    //  전시회 지역+상태 검색
    @Operation(summary = "공연(전시회) 게시글 지역, 상태, 검색 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchExhibits(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = exhibitService
                .searchExhibits(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

}
