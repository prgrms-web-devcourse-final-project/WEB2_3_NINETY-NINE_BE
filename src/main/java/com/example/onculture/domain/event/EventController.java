package com.example.onculture.domain.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    //팝업 스토어 목록 조회
    @GetMapping("/popups")
    public List<EventDTO> getPopupStore() {
        return Arrays.asList(
                new EventDTO(1L,
                        "팝업스토어이름",
                        "팝업스토어",
                        "2025-02-15",
                        "2025-02-17",
                        127.0276,
                        37.4979,
                        10,
                        "종료",
                        "10:00 AM - 08:00 PM",
                        "팝업스토어"
                )
        );
    }
    //전시회 목록 조회
    @GetMapping("/exhibition")
    public List<EventDTO> getExhibition() {
        return Arrays.asList(
                new EventDTO(1L,
                        "전시회이름",
                        "전시회",
                        "2025-02-15",
                        "2025-02-17",
                        127.0276,
                        37.4979,
                        10,
                        "종료",
                        "10:00 AM - 08:00 PM",
                        "전시회"
                )
        );
    }

    //뮤지컬 목록 조회
    @GetMapping("/musical")
    public List<EventDTO> getMusical() {
        return Arrays.asList(
                new EventDTO(1L,
                        "뮤지컬/연극 이름",
                        "뮤지컬/연극",
                        "2025-02-15",
                        "2025-02-17",
                        127.0276,
                        37.4979,
                        10,
                        "종료",
                        "10:00 AM - 08:00 PM",
                        "뮤지컬/연극"
                )
        );
    }

    //콘서트 목록 조회
    @GetMapping("/concert")
    public List<EventDTO> getConcert() {
        return Arrays.asList(
                new EventDTO(1L,
                        "콘서트이름",
                        "콘서트",
                        "2025-02-15",
                        "2025-02-17",
                        127.0276,
                        37.4979,
                        10,
                        "종료",
                        "10:00 AM - 08:00 PM",
                        "콘서트"
                )
        );
    }

    //카테고리별 검색
    @GetMapping("/search")
    public List<EventDTO> getSearch(@RequestParam(required = false) String eventTitle,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate ) {
        return Arrays.asList(
                new EventDTO(1L, eventTitle, category , startDate,endDate,127.0276,37.4979,10,"진행중", "10:00 AM - 08:00 PM",category + " 설명")
        );

    }

}