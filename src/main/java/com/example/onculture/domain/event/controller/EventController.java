package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.dto.EventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Tag(name = "event", description = "행사 API")
@RestController
@RequestMapping("/api/events")
public class EventController {

    //더미데이터
    private List<EventDTO> eventList = Arrays.asList(
            new EventDTO(1L, "팝업스토어1", "팝업스토어", "2025-02-15", "2025-02-17", 127.0276, 37.4979, 10, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명"),
            new EventDTO(2L, "전시회1", "전시회", "2025-02-16", "2025-02-18", 127.0300, 37.4990, 20, "오픈예정", "09:00 AM - 06:00 PM", "전시회 설명"),
            new EventDTO(3L, "뮤지컬1", "뮤지컬", "2025-02-18", "2025-02-20", 127.0320, 37.4950, 30, "진행중", "12:00 PM - 10:00 PM", "뮤지컬 설명"),
            new EventDTO(4L, "콘서트1", "콘서트", "2025-02-20", "2025-02-22", 127.0290, 37.4980, 40, "진행종료", "08:00 PM - 11:00 PM", "콘서트 설명"),
            new EventDTO(5L, "팝업스토어2", "팝업스토어", "2025-02-17", "2025-02-19", 127.0330, 37.4960, 50, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명 2"),
            new EventDTO(6L, "전시회2", "전시회", "2025-02-14", "2025-02-16", 127.0250, 37.4920, 60, "진행중", "10:00 AM - 07:00 PM", "전시회 설명 2"),
            new EventDTO(7L, "뮤지컬2", "뮤지컬", "2025-02-19", "2025-02-21", 127.0280, 37.4940, 70, "오픈예정", "11:00 AM - 09:00 PM", "뮤지컬 설명 2"),
            new EventDTO(8L, "콘서트2", "콘서트", "2025-02-22", "2025-02-24", 127.0295, 37.4975, 80, "진행종료", "09:00 PM - 12:00 AM", "콘서트 설명 2")
    );

    //전체 목록 조회
    @Operation(summary = "모든 데이터 호출",
            description = "모든 데이터를 호출합니다.")
    @GetMapping("all")
    public List<EventDTO> getAllEvents() {
        return eventList;
    }

    //팝업 스토어 목록 조회
    @Operation(summary = "팝업스토어 목록 조회",
            description = "팝업스토어 목록을 조회합니다.")
    @GetMapping("/popups")
    public List<EventDTO> getPopupStore() {
       List<EventDTO> popupStore = new ArrayList<>();

       for (EventDTO eventDTO : eventList) {
           if("팝업스토어".equals(eventDTO.getCategory())){
               popupStore.add(eventDTO);
           }
       }
        return popupStore;
    }

    //전시회 목록 조회
    @Operation(summary = "전시회 목록 조회",
            description = "전시회 목록을 조회합니다.")
    @GetMapping("/exhibition")
    public List<EventDTO> getExhibition() {
        List<EventDTO> exhibition = new ArrayList<>();

        for (EventDTO eventDTO : eventList) {
            if("전시회".equals(eventDTO.getCategory())){
                exhibition.add(eventDTO);
            }
        }
        return exhibition;
    }


    //뮤지컬 목록 조회
    @Operation(summary = "뮤지컬 목록 조회",
            description = "뮤지컬 목록을 조회합니다.")
    @GetMapping("/musical")
    public List<EventDTO> getMusical() {
        List<EventDTO> musical = new ArrayList<>();

        for (EventDTO eventDTO : eventList) {
            if("뮤지컬".equals(eventDTO.getCategory())){
                musical.add(eventDTO);
            }
        }
        return musical;
    }

    //콘서트 목록 조회
    @Operation(summary = "콘서트 목록 조회",
            description = "콘서트 목록을 조회합니다.")
    @GetMapping("/concert")
    public List<EventDTO> getConcert() {
        List<EventDTO> concert = new ArrayList<>();

        for (EventDTO eventDTO : eventList) {
            if("콘서트".equals(eventDTO.getCategory())){
                concert.add(eventDTO);
            }
        }
        return concert;
    }

    //북마크 목록 조회
    private List<Long> bookmarkList = new ArrayList<>();

    //북마크 추가
    @Operation(summary = "북마크를 추가합니다.",
            description = "")
    @PostMapping("/bookmark")
    public String addBookmark(@RequestParam Long eventId){
        //북마크 중복 체크 후 추가
        if(bookmarkList.contains(eventId)){
            return "이미 추가된 이벤트 입니다.";
        }else {
            bookmarkList.add(eventId);
            return "북마크 추가 완료";
        }
    }

    // 북마크 삭제
    @Operation(summary = "북마크를 삭제합니다.",
            description = "북마크에서 특정 이벤트를 삭제합니다.")
    @DeleteMapping("/bookmark")
    public String removeBookmark(@RequestParam Long eventId) {
        //북마크 체크 유뮤 확인후 삭제
        if (bookmarkList.contains(eventId)) {
            bookmarkList.remove(eventId);
            return "북마크 삭제 완료";
        } else {
            return "북마크에 없는 이벤트입니다.";
        }
    }

    //공유하기 (실행만)
    @Operation(summary = " 공유하기 ",
            description = "")
    @PostMapping("share")
    public String shareEvent(@RequestParam Long eventId) {
        return  "이벤트 ID : " + eventId + " 공유되었습니다.";
    }

    //공연 이름, 카테고리, 시작일자 검색
    @Operation(summary = "공연 이름, 카테고리, 시작일자 검색",
            description = "공연 이름, 카테고리, 시작일자 검색합니다. ")
    @GetMapping("/search")
    public List<EventDTO> getSearch(@RequestParam(required = false) String eventTitle,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String status ) {
        return Arrays.asList(
                new EventDTO(1L, eventTitle, category , startDate,"2025-02-17",127.0276,37.4979,10,status, "10:00 AM - 08:00 PM",category + " 설명")
        );

    }

    //상태별, 지역별 정렬 (현재는 위도 기준 - 지도 API 를 사용해서 **동 or **구 등으로 변경)
    @Operation(summary = "상태별, 지역별 정렬",
            description = "상태별, 지역별 정렬합니다.")
    @GetMapping("/sort")
    public List<EventDTO> sortEvents(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String sortBy) {
        // 상태별 필터링
        List<EventDTO> filteredEvents = new ArrayList<>();
        for (EventDTO event : eventList) {
            if (status == null || event.getEvent_status().equals(status)) {
                filteredEvents.add(event);
            }
        }

        if ("location".equals(sortBy)) {
            // 위도 기준 정렬 (지역별)
            filteredEvents.sort(Comparator.comparingDouble(EventDTO::getLatitude));
        } else if ("status".equals(sortBy)) {
            // 상태 기준 정렬 (진행 상태별)
            filteredEvents.sort(Comparator.comparing(EventDTO::getEvent_status));
        }
        return filteredEvents;
    }
}
