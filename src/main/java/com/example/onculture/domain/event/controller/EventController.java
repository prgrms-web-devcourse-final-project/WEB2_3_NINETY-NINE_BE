package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.FestivalPostDTO;
import com.example.onculture.domain.event.dto.PopupStorePostDTO;
import com.example.onculture.domain.event.service.FestivalPostAddressUpdateService;
import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
import com.example.onculture.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 전체 PopupStore 목록 조회
    @Operation(summary = "전체 PopupStore 목록 조회",
            description = "전체 PopupStore 목록을 조회합니다.")
    @GetMapping("/popup-store-posts")
    public List<PopupStorePost> listAllPopupStorePosts() {
        return popupStorePostService.listAll();
    }

    // 제목(title)를 이용한 PopupStore 검색 (실제로 content 필드를 검색)
    @Operation(summary = "제목(title)를 이용한 PopupStore 검색",
            description = "제목(title)를 이용하여 PopupStore 게시글을 검색합니다. 요청 항목: title(검색어)")
    @GetMapping("/popup-store-posts/title")
    public List<PopupStorePost> searchPopupStorePosts(@RequestParam("title") String title) {
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        return popupStorePostService.searchByTitle(title);
    }

    //랜덤 조회
    @GetMapping("/popup-store-posts/random")
    public ResponseEntity<SuccessResponse<List<PopupStorePostDTO>>> getRandomPopupStorePosts(
            @RequestParam(defaultValue = "9") int randomSize) {
        List<PopupStorePostDTO> dtos = popupStorePostService.getRandomPopupStorePosts(randomSize);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, dtos));
    }
    //상세 조회
    @Operation(summary = "팝업 스토어 데이터 상세 정보 조회",
            description = "팝업 스토어 데이터 상세 정보 조회")
    @GetMapping("/popup-store/detail")
    public ResponseEntity<EventResponseDTO> getPopupStorePostDetail(@RequestParam Long id) {
        EventResponseDTO detail = popupStorePostService.getPopupStorePostDetail(id);
        return ResponseEntity.ok(detail);
    }


    // 전체 Festival 목록 조회
    @Operation(summary = "전체 Festival 목록 조회",
            description = "전체 Festival 게시글 목록을 조회합니다.")
    @GetMapping("/festival-posts")
    public List<FestivalPost> listAllFestivalPosts() {
        return festivalPostService.listAll();
    }

    // 제목(title)를 이용한 Festival 검색 (festival_content 필드 검색)
    @Operation(summary = "제목(title)를 이용한 Festival 검색",
            description = "제목(title)를 이용하여 Festival 게시글을 검색합니다. 요청 항목: title(검색어)")
    @GetMapping("/festival-posts/title")
    public List<FestivalPost> searchFestivalPosts(@RequestParam("title") String title) {
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        return festivalPostService.searchByTitle(title);
    }
    // festival 랜덤 조회
    @GetMapping("/festival-posts/random")
    public ResponseEntity<SuccessResponse<List<FestivalPostDTO>>> getRandomFestivalPosts(
            @RequestParam(defaultValue = "9") int randomSize) {
        List<FestivalPostDTO> dtos = festivalPostService.getRandomFestivalPosts(randomSize);
        return ResponseEntity.ok(SuccessResponse.success(HttpStatus.OK, dtos));
    }
    //상세 조회
    @Operation(summary = "팝업 스토어 데이터 상세 정보 조회",
            description = "팝업 스토어 데이터 상세 정보 조회")
    @GetMapping("/festival/detail")
    public ResponseEntity<EventResponseDTO> getFestivalPostDetail(@RequestParam Long id) {
        EventResponseDTO detail = festivalPostService.getFestivalPostDetail(id);
        return ResponseEntity.ok(detail);
    }

    //DB에 저장된 FestivalPost의 festivalLocation 필드를 업데이트한 후 전체 목록을 반환합니다.
    @GetMapping("/update-addresses")
    public ResponseEntity<List<FestivalPost>> updateAddresses() {
        List<FestivalPost> updatedPosts = festivalPostAddressUpdateService.updateFestivalPostAddressesAndAreas();
        return ResponseEntity.ok(updatedPosts);
    }

}