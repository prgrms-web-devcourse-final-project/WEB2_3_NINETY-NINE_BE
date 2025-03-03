package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.PerformanceService;
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
@RequestMapping("/api/performances")
@Tag(name = "공연(뮤지컬,연극) API", description = "공연(뮤지컬,연극)을 관리하는 API")
public class PerformanceController {
    private final PerformanceService performanceService;

    @Operation(summary = "공연(뮤지컬,연극) 저장",
            description = "KOPIS OPEN API를 사용해서 데이터를 저장하는 API 입니다.")
    @PostMapping("/save")
    public void saveKOPISPerformances(@RequestParam String from, @RequestParam String to, @RequestParam String genre, @RequestParam String status) {
        performanceService.savePerformances(from, to, genre, status);
    }

    @Operation(summary = "공연(뮤지컬,연극) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomPerformances(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = performanceService.getRandomPerformances(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    @Operation(summary = "공연(뮤지컬,연극) 게시글 지역, 상태, 검색 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchPerformances(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = performanceService
                .searchPerformances(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    @Operation(summary = "공연(뮤지컬,연극) 게시글 상세 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/{performanceId}")
    public ResponseEntity<SuccessResponse<EventResponseDTO>> getPerformance(
            @PathVariable Long performanceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO responseDTO = performanceService
                .getPerformance(performanceId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO));
    }
}
