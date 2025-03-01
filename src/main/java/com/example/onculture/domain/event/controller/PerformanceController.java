package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.service.PerformanceService;
import com.example.onculture.global.response.SuccessResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class PerformanceController {
    private final PerformanceService performanceService;

    @PostMapping("api/performances/save")
    public void saveKOPISPerformances(@RequestParam String from, @RequestParam String to, @RequestParam String genre, @RequestParam String status) {
        performanceService.savePerformances(from, to, genre, status);
    }

    @GetMapping("api/performances/random")
    public ResponseEntity<SuccessResponse<List<EventResponseDTO>>> getPerformances(
            @RequestParam(defaultValue = "9") int randomSize) {
        List<EventResponseDTO> responseDTOS = performanceService.getRandomPerformances(randomSize);
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, responseDTOS));
    }

//    @GetMapping("api/performances")
//    public void getPerformances(
//            @RequestParam String region,
//            @RequestParam String status,
//            @RequestParam String titleKeyword,
//            @RequestParam String pageNum,
//            @RequestParam String pageSize) {
//        performanceService.getPerformances(region, status, titleKeyword, pageNum, pageSize);
//    }
}
