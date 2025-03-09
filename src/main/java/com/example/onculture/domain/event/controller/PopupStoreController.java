package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.PopupStoreService;
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
@RequestMapping("/api/events/popupstores")
@Tag(name = "팝업스토어 API", description = "팝업스토어를 관리하는 API")
public class PopupStoreController {
    private final PopupStoreService popupStoreService;

    //랜덤 조회
    @Operation(summary = "공연(PopupStore) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomPopupStorePosts(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = popupStoreService.getRandomPopupStorePosts(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    //상세 조회
    @Operation(summary = "팝업 스토어 데이터 상세 정보 조회",
            description = "팝업 스토어 데이터 상세 정보 조회")
    @GetMapping("/{popupstoreId}")
    public ResponseEntity<EventResponseDTO> getPopupStorePostDetail(
            @PathVariable Long popupstoreId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO responseDTO = popupStoreService
                .getPopupStorePostDetail(popupstoreId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTO).getData());
    }

    //  팝업 스토어 지역+상태 검색
    @Operation(summary = "공연(PopupStore) 지역+상태 검색 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchPopupStorePosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = popupStoreService
                .searchPopupStorePosts(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    @Operation(summary = "PopStore Crawling 실행",
            description = "PopStore Crawling 실행")
    @GetMapping("/crawl")
    public String crawlPopUpStore() {
        try {
            popupStoreService.runCrawling();
            return "PopUpStore Crawling 완료";
        } catch (Exception e) {
            return "PopUpStore Crawling 오류: " + e.getMessage();
        }
    }
}
