package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.service.FestivalPostService;
import com.example.onculture.domain.event.service.PopupStorePostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class CrawlControllerTest {

    @Mock
    private PopupStorePostService popupStorePostService;

    @Mock
    private FestivalPostService festivalPostService;

    @InjectMocks
    private CrawlController crawlController;

    @Test
    @DisplayName("crawlPopUpStore - 성공: 정상 실행 시 'PopUpStore Crawling 완료' 반환")
    void testCrawlPopUpStoreSuccess() {
        // arrange: popupStorePostService.runCrawling()가 예외 없이 정상 실행되도록 설정
        doNothing().when(popupStorePostService).runCrawling();

        // act
        String result = crawlController.crawlPopUpStore();

        // assert
        assertEquals("PopUpStore Crawling 완료", result);
    }

    @Test
    @DisplayName("crawlPopUpStore - 실패: 예외 발생 시 오류 메시지 반환")
    void testCrawlPopUpStoreFailure() {
        // arrange: popupStorePostService.runCrawling() 호출 시 예외 발생하도록 설정
        String errorMsg = "Test Exception";
        doThrow(new RuntimeException(errorMsg)).when(popupStorePostService).runCrawling();

        // act
        String result = crawlController.crawlPopUpStore();

        // assert
        assertEquals("PopUpStore Crawling 오류: " + errorMsg, result);
    }

    @Test
    @DisplayName("crawlFestival - 성공: 정상 실행 시 'Festival Crawling 완료' 반환")
    void testCrawlFestivalSuccess() {
        // arrange: festivalPostService.runCrawling()가 예외 없이 실행되도록 설정
        doNothing().when(festivalPostService).runCrawling();

        // act
        String result = crawlController.crawlFestival();

        // assert
        assertEquals("Festival Crawling 완료", result);
    }

    @Test
    @DisplayName("crawlFestival - 실패: 예외 발생 시 오류 메시지 반환")
    void testCrawlFestivalFailure() {
        // arrange: festivalPostService.runCrawling() 호출 시 예외 발생하도록 설정
        String errorMsg = "Festival Error";
        doThrow(new RuntimeException(errorMsg)).when(festivalPostService).runCrawling();

        // act
        String result = crawlController.crawlFestival();

        // assert
        assertEquals("Festival Crawling 오류: " + errorMsg, result);
    }
}
