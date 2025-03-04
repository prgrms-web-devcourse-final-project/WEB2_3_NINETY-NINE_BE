package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.FestivalPostDTO;
import com.example.onculture.domain.event.dto.PopupStorePostDTO;
import com.example.onculture.domain.event.service.FestivalPostAddressUpdateService;
import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final PopupStorePostService popupStorePostService;
    private final FestivalPostService festivalPostService;
    private final FestivalPostAddressUpdateService festivalPostAddressUpdateService;


    public EventController(PopupStorePostService popupStorePostService, FestivalPostService festivalPostService, FestivalPostAddressUpdateService festivalPostAddressUpdateService) {
        this.popupStorePostService = popupStorePostService;
        this.festivalPostService = festivalPostService;
        this.festivalPostAddressUpdateService = festivalPostAddressUpdateService;
    }

    // 공연(PopupStore) 목록 조회
    @Operation(summary = "공연(PopupStore) 목록 조회",
            description = "공연(PopupStore) 목록을 조회합니다.")
    @GetMapping("/popup-store-posts")
    public List<PopupStorePost> listAllPopupStorePosts() {
        return popupStorePostService.listAll();
    }

    // 제목(title)를 이용한 PopupStore 검색 (실제로 content 필드를 검색)
    @Operation(summary = "제목(title)를 이용한 공연(PopupStore)검색",
            description = "제목(title)를 이용하여 공연(PopupStore) 게시글을 검색합니다. 요청 항목: title(검색어)")
    @GetMapping("/popup-store-posts/title")
    public List<PopupStorePost> searchPopupStorePosts(@RequestParam("title") String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        return popupStorePostService.searchByTitle(title);
    }

    //랜덤 조회
    @Operation(summary = "공연(PopupStore) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/popup-store-posts/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomPopupStorePosts(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = popupStorePostService.getRandomPopupStorePosts(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

    //상세 조회
    @Operation(summary = "팝업 스토어 데이터 상세 정보 조회",
            description = "팝업 스토어 데이터 상세 정보 조회")
    @GetMapping("/popup-store/detail")
    public ResponseEntity<EventResponseDTO> getPopupStorePostDetail(
            @RequestParam Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO detail = popupStorePostService.getPopupStorePostDetail(id, userId);
        return ResponseEntity.ok(detail);
    }

    //  팝업 스토어 지역+상태 검색
    @Operation(summary = "공연(PopupStore) 지역+상태 검색 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/search_popup_store")
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchPopupStorePosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = popupStorePostService
                .searchPopupStorePosts(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }


    // 전체 Festival 목록 조회
    @Operation(summary = "전체 공연(festival) 목록 조회",
            description = "전체 공연(festival) 게시글 목록을 조회합니다.")
    @GetMapping("/festival-posts")
    public List<FestivalPost> listAllFestivalPosts() {
        return festivalPostService.listAll();
    }

    // 제목(title)를 이용한 Festival 검색 (festival_content 필드 검색)
    @Operation(summary = "제목(title)를 이용한 Festival 검색",
            description = "제목(title)를 이용하여 Festival 게시글을 검색합니다. 요청 항목: title(검색어)")
    @GetMapping("/festival-posts/title")
    public List<FestivalPost> searchFestivalPosts(@RequestParam("title") String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        return festivalPostService.searchByTitle(title);
    }

    // festival 랜덤 조회
    @Operation(summary = "공연(festival) 게시글 랜덤 조회",
            description = "randomSize를 입력하셔야 되고 로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/festival-posts/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getRandomFestivalPosts(
            @RequestParam(defaultValue = "9") int randomSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<EventResponseDTO> responseDTOS = festivalPostService.getRandomFestivalPosts(randomSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }


    //상세 조회
    @Operation(summary = "공연(festival) 데이터 상세 정보 조회",
            description = "로그인을 한 유저면 토큰을 담아서 요청하셔야 북마크 여부를 알 수 있습니다.")
    @GetMapping("/festival/detail")
    public ResponseEntity<EventResponseDTO> getFestivalPostDetail(
            @RequestParam Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventResponseDTO detail = festivalPostService.getFestivalPostDetail(id, userId);
        return ResponseEntity.ok(detail);
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
    @GetMapping("/search_festival")
    public ResponseEntity<SuccessResponse<EventPageResponseDTO>> searchFestivalPosts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "9") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        EventPageResponseDTO responseDTOS = festivalPostService
                .searchFestivalPosts(region, status, titleKeyword, pageNum, pageSize, userId);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }
}