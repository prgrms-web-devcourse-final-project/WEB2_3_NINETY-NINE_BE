package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    private final PopupStorePostService popupStorePostService;
    private final FestivalPostService festivalPostService;

    public CrawlController(PopupStorePostService popupStorePostService,
                           FestivalPostService festivalPostService) {
        this.popupStorePostService = popupStorePostService;
        this.festivalPostService = festivalPostService;
    }

    //PopupStore crawling 실행
    @Operation(summary = "PopStore Crawling 실행",
            description = "PopStore Crawling 실행")
    @GetMapping("/popup-store")
    public String crawlPopUpStore() {
        try {
            popupStorePostService.runCrawling();
            return "PopUpStore Crawling 완료";
        } catch (Exception e) {
            return "PopUpStore Crawling 오류: " + e.getMessage();
        }
    }

    //Festival crawling 실행
    @Operation(summary = "Festival Crawling 실행",
            description = "Festival Crawling 실행")
    @GetMapping("/festival")
    public String crawlFestival() {
        try {
            festivalPostService.runCrawling();
            return "Festival Crawling 완료";
        } catch (Exception e) {
            return "Festival Crawling 오류: " + e.getMessage();
        }
    }
}
