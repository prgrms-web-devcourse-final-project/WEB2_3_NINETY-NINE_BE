package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.model.PopupStorePost;
import com.example.onculture.domain.event.service.PopupStorePostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api")
public class EventController {
    private final PopupStorePostService popupStorePostService;

    public EventController(PopupStorePostService popupStorePostService) {
        this.popupStorePostService = popupStorePostService;
    }

    // 전체 PopStore 목록 조회
    @Operation(summary = "전체 PopStore 목록 조회",
            description = "전체 PopStore 목록 조회.")
    @GetMapping("/popup-store-posts")
    public List<PopupStorePost> listAll() {
        return popupStorePostService.listAll();
    }

    // 위치(keyword)를 이용한 검색
    @Operation(summary = "위치(keyword)를 이용한 PopupStore 검색",
            description = "위치(keyword)를 이용한 PopupStore 검색. 요청 항목 : keyword(서울, 경기)")
    @GetMapping("/popup-store-posts/search")
    public List<PopupStorePost> search(@RequestParam("keyword") String keyword) {
        return popupStorePostService.searchByLocation(keyword);
    }
}
