package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.model.FestivalPost;
import com.example.onculture.domain.event.model.PopupStorePost;
import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
import io.swagger.v3.oas.annotations.Operation;
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

    public EventController(PopupStorePostService popupStorePostService, FestivalPostService festivalPostService) {
        this.popupStorePostService = popupStorePostService;
        this.festivalPostService = festivalPostService;
    }

    // 전체 PopupStore 목록 조회
    @Operation(summary = "전체 PopStore 목록 조회",
            description = "전체 PopStore 목록 조회.")
    @GetMapping("/popup-store-posts")
    public List<PopupStorePost> listAllPopupStorePosts() {
        return popupStorePostService.listAll();
    }

    // 위치(keyword)를 이용한 PopupStore 검색 (서울 , 고양 검색 가능)
    @Operation(summary = "위치(keyword)를 이용한 PopupStore 검색",
            description = "위치(keyword)를 이용한 PopupStore 검색. 요청 항목 : keyword(서울, 경기)")
    @GetMapping("/popup-store-posts/search")
    public List<PopupStorePost> searchPopupStorePosts(@RequestParam("keyword") String keyword) {
        return popupStorePostService.searchByLocation(keyword);
    }

    // 전체 Festival 목록 조회
    @Operation(summary = "전체 Festival 목록 조회",
            description = "전체 Festival 목록 조회.")
    @GetMapping("/festival-posts")
    public List<FestivalPost> listAllFestivalPosts() {
        return festivalPostService.listAll();
    }

    // 위치(keyword)를 이용한 Festival 검색 (킨텍스, 지역 검색이 안됨..)
    @Operation(summary = "위치(keyword)를 이용한 Festival 검색",
            description = "위치(keyword)를 이용한 Festival 검색. 요청 항목 : keyword(서울, 경기)")
    @GetMapping("/festival-posts/search")
    public List<FestivalPost> searchFestivalPosts(@RequestParam("keyword") String keyword) {
        return festivalPostService.searchByLocation(keyword);
    }
}
