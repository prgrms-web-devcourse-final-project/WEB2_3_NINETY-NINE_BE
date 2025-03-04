package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.dto.ExhibitDetailDTO;
import com.example.onculture.domain.event.service.ExhibitService;
import com.example.onculture.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibit")
@RequiredArgsConstructor
public class ExhibitController {

    private final ExhibitService exhibitService;

    // 공공 데이터 API 저장
    @Operation(summary = "전시회 공공 데이터 저장",
            description = "전시회 공공 데이터 저장")
    @GetMapping("/fetchXmlAndSave")
    public ResponseEntity<String> fetchXmlAndSave() {
        exhibitService.fetchXmlAndSaveWithPagination();
        return ResponseEntity.ok("공공데이터 XML을 불러와 저장했습니다.");
    }

    // 기간별 전시 목록 조회
    @Operation(summary = "기간 내의 전시회 데이터 목록 조회",
            description = "기간 내의 전시회 데이터 목록 조회")
    @GetMapping("/period")
    public ResponseEntity<List<ExhibitDTO>> getExhibitionsByPeriod(
            @RequestParam String from,
            @RequestParam String to) {
        List<ExhibitDTO> list = exhibitService.getExhibitByPeriod(from, to);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    // 지역별 전시 목록 조회
    @Operation(summary = "지역별 전시회 데이터 목록 조회",
            description = "지역별 전시회 데이터 목록 조회")
    @GetMapping("/area")
    public ResponseEntity<List<ExhibitDTO>> getExhibitionsByArea(
            @RequestParam String sido,  // 시/도 (예: 서울)
            @RequestParam(required = false) String gugun,  // 군/구
            @RequestParam String from,
            @RequestParam String to) {
        List<ExhibitDTO> list = exhibitService.getExhibitByArea(sido, from, to);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    // 분야별 전시 목록 조회
    @Operation(summary = "분야별 전시회 데이터 목록 조회",
            description = "분야별 전시회 데이터 목록 조회")
    @GetMapping("/realm")
    public ResponseEntity<List<ExhibitDTO>> getExhibitionsByRealm(
            @RequestParam String realmCode,  // 분야 "전시"
            @RequestParam String from,
            @RequestParam String to) {
        List<ExhibitDTO> list = exhibitService.getExhibitByRealm(realmCode, from, to);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    // 전시 상세 정보 조회
    @Operation(summary = "전시회 데이터 상세 정보 조회",
            description = "전시회 데이터 상세 정보 조회")
    @GetMapping("/detail")
    public ResponseEntity<EventResponseDTO> getExhibitionDetail(@RequestParam Long seq) {
        EventResponseDTO detail = exhibitService.getExhibitDetail(seq);
        return ResponseEntity.ok(detail);
    }

    // 제목 검색
    @GetMapping("/title")
    public List<ExhibitDTO> getExhibitByTitle(@RequestParam String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        return exhibitService.getExhibitByTitle(title.trim());
    }

    //랜덤 조회
    @GetMapping("/random")
    public ResponseEntity<List<ExhibitDTO>> getRandomExhibitions(
            @RequestParam(defaultValue = "9") int randomSize) {
        List<ExhibitDTO> list = exhibitService.getRandomExhibitions(randomSize);
        return ResponseEntity.ok(list);
    }
    //  전시회 지역+상태 검색
    @GetMapping("/search_exhibits")
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchExhibits(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize) {
        EventPageResponseDTO responseDTOS = exhibitService
                .searchExhibits(region, status, titleKeyword, pageNum, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

}
