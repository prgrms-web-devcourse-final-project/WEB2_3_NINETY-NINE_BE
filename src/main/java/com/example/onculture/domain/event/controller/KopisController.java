package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.service.KopisService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class KopisController {
    private final KopisService kopisService;

    @PostMapping("api/kopis/performances/save")
    public void saveKOPISPerformances(@RequestParam String from, @RequestParam String to, @RequestParam String genre, @RequestParam String status) {
        kopisService.savePerformances(from, to, genre, status);
    }
}
