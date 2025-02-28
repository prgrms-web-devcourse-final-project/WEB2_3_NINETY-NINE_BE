package com.example.onculture.domain.event;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.example.onculture.domain.socialPost.dto.PostResponseDTO;

@Tag(name = "event", description = "행사 API")
@RestController
@RequestMapping("/api/events")
public class EventController {

    //더미데이터
    private List<EventDTO> eventList = Arrays.asList(
        new EventDTO(1L, "팝업스토어1", "팝업스토어", "2025-02-15", "2025-02-17", 127.0276, 37.4979, 10, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명"),
        new EventDTO(2L, "팝업스토어2", "팝업스토어", "2025-02-17", "2025-02-19", 127.0330, 37.4960, 50, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명 2"),
        new EventDTO(3L, "팝업스토어3", "팝업스토어", "2025-02-20", "2025-02-22", 127.0300, 37.4975, 20, "오픈예정", "09:00 AM - 06:00 PM", "팝업스토어 설명 3"),
        new EventDTO(4L, "팝업스토어4", "팝업스토어", "2025-02-23", "2025-02-25", 127.0290, 37.4955, 30, "진행중", "10:00 AM - 07:00 PM", "팝업스토어 설명 4"),
        new EventDTO(5L, "팝업스토어5", "팝업스토어", "2025-02-25", "2025-02-27", 127.0280, 37.4965, 40, "진행종료", "11:00 AM - 09:00 PM", "팝업스토어 설명 5"),
        new EventDTO(6L, "팝업스토어6", "팝업스토어", "2025-02-28", "2025-03-02", 127.0270, 37.4970, 60, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명 6"),
        new EventDTO(7L, "팝업스토어7", "팝업스토어", "2025-03-01", "2025-03-03", 127.0265, 37.4985, 70, "오픈예정", "09:00 AM - 06:00 PM", "팝업스토어 설명 7"),
        new EventDTO(8L, "팝업스토어8", "팝업스토어", "2025-03-04", "2025-03-06", 127.0295, 37.4950, 80, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명 8"),
        new EventDTO(9L, "팝업스토어9", "팝업스토어", "2025-03-05", "2025-03-07", 127.0305, 37.4965, 90, "진행종료", "11:00 AM - 09:00 PM", "팝업스토어 설명 9"),
        new EventDTO(10L, "팝업스토어10", "팝업스토어", "2025-03-06", "2025-03-08", 127.0320, 37.4975, 100, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명 10"),

        new EventDTO(11L, "전시회1", "전시회", "2025-02-16", "2025-02-18", 127.0300, 37.4990, 20, "오픈예정", "09:00 AM - 06:00 PM", "전시회 설명"),
        new EventDTO(12L, "전시회2", "전시회", "2025-02-14", "2025-02-16", 127.0250, 37.4920, 60, "진행중", "10:00 AM - 07:00 PM", "전시회 설명 2"),
        new EventDTO(13L, "전시회3", "전시회", "2025-02-18", "2025-02-20", 127.0320, 37.4955, 30, "진행중", "09:00 AM - 06:00 PM", "전시회 설명 3"),
        new EventDTO(14L, "전시회4", "전시회", "2025-02-20", "2025-02-22", 127.0315, 37.4960, 40, "진행종료", "09:00 AM - 05:00 PM", "전시회 설명 4"),
        new EventDTO(15L, "전시회5", "전시회", "2025-02-22", "2025-02-24", 127.0295, 37.4950, 50, "진행중", "10:00 AM - 07:00 PM", "전시회 설명 5"),
        new EventDTO(16L, "전시회6", "전시회", "2025-02-23", "2025-02-25", 127.0290, 37.4970, 70, "오픈예정", "11:00 AM - 09:00 PM", "전시회 설명 6"),
        new EventDTO(17L, "전시회7", "전시회", "2025-02-24", "2025-02-26", 127.0285, 37.4945, 80, "진행중", "09:00 AM - 06:00 PM", "전시회 설명 7"),
        new EventDTO(18L, "전시회8", "전시회", "2025-02-25", "2025-02-27", 127.0305, 37.4955, 90, "진행종료", "10:00 AM - 08:00 PM", "전시회 설명 8"),
        new EventDTO(19L, "전시회9", "전시회", "2025-02-27", "2025-03-01", 127.0330, 37.4960, 100, "진행중", "11:00 AM - 09:00 PM", "전시회 설명 9"),
        new EventDTO(20L, "전시회10", "전시회", "2025-03-01", "2025-03-03", 127.0310, 37.4950, 110, "오픈예정", "09:00 AM - 06:00 PM", "전시회 설명 10"),

        new EventDTO(21L, "뮤지컬1", "뮤지컬", "2025-02-18", "2025-02-20", 127.0320, 37.4950, 30, "진행중", "12:00 PM - 10:00 PM", "뮤지컬 설명"),
        new EventDTO(22L, "뮤지컬2", "뮤지컬", "2025-02-19", "2025-02-21", 127.0280, 37.4940, 70, "오픈예정", "11:00 AM - 09:00 PM", "뮤지컬 설명 2"),
        new EventDTO(23L, "뮤지컬3", "뮤지컬", "2025-02-21", "2025-02-23", 127.0295, 37.4935, 40, "진행종료", "09:00 AM - 11:00 PM", "뮤지컬 설명 3"),
        new EventDTO(24L, "뮤지컬4", "뮤지컬", "2025-02-22", "2025-02-24", 127.0275, 37.4955, 50, "진행중", "10:00 AM - 08:00 PM", "뮤지컬 설명 4"),
        new EventDTO(25L, "뮤지컬5", "뮤지컬", "2025-02-24", "2025-02-26", 127.0300, 37.4940, 60, "오픈예정", "11:00 AM - 09:00 PM", "뮤지컬 설명 5"),
        new EventDTO(26L, "뮤지컬6", "뮤지컬", "2025-02-25", "2025-02-27", 127.0315, 37.4960, 80, "진행중", "09:00 AM - 07:00 PM", "뮤지컬 설명 6"),
        new EventDTO(27L, "뮤지컬7", "뮤지컬", "2025-02-26", "2025-02-28", 127.0325, 37.4970, 90, "진행종료", "10:00 AM - 08:00 PM", "뮤지컬 설명 7"),
        new EventDTO(28L, "뮤지컬8", "뮤지컬", "2025-02-28", "2025-03-02", 127.0335, 37.4980, 100, "진행중", "11:00 AM - 09:00 PM", "뮤지컬 설명 8"),
        new EventDTO(29L, "뮤지컬9", "뮤지컬", "2025-03-01", "2025-03-03", 127.0295, 37.4955, 110, "오픈예정", "09:00 AM - 06:00 PM", "뮤지컬 설명 9"),
        new EventDTO(30L, "뮤지컬10", "뮤지컬", "2025-03-03", "2025-03-05", 127.0310, 37.4935, 120, "진행중", "10:00 AM - 08:00 PM", "뮤지컬 설명 10"),

        new EventDTO(31L, "콘서트1", "콘서트", "2025-02-20", "2025-02-22", 127.0290, 37.4980, 40, "진행종료", "08:00 PM - 11:00 PM", "콘서트 설명"),
        new EventDTO(32L, "콘서트2", "콘서트", "2025-02-22", "2025-02-24", 127.0295, 37.4975, 80, "진행종료", "09:00 PM - 12:00 AM", "콘서트 설명 2"),
        new EventDTO(33L, "콘서트3", "콘서트", "2025-02-23", "2025-02-25", 127.0300, 37.4965, 90, "진행중", "10:00 PM - 01:00 AM", "콘서트 설명 3"),
        new EventDTO(34L, "콘서트4", "콘서트", "2025-02-24", "2025-02-26", 127.0310, 37.4950, 100, "오픈예정", "11:00 PM - 02:00 AM", "콘서트 설명 4"),
        new EventDTO(35L, "콘서트5", "콘서트", "2025-02-25", "2025-02-27", 127.0335, 37.4970, 110, "진행중", "08:00 PM - 11:00 PM", "콘서트 설명 5"),
        new EventDTO(36L, "콘서트6", "콘서트", "2025-02-26", "2025-02-28", 127.0325, 37.4960, 120, "진행종료", "10:00 PM - 01:00 AM", "콘서트 설명 6"),
        new EventDTO(37L, "콘서트7", "콘서트", "2025-02-27", "2025-03-01", 127.0330, 37.4955, 130, "오픈예정", "11:00 PM - 02:00 AM", "콘서트 설명 7"),
        new EventDTO(38L, "콘서트8", "콘서트", "2025-02-28", "2025-03-02", 127.0340, 37.4985, 140, "진행중", "09:00 PM - 12:00 AM", "콘서트 설명 8"),
        new EventDTO(39L, "콘서트9", "콘서트", "2025-03-01", "2025-03-03", 127.0320, 37.4950, 150, "진행종료", "10:00 PM - 01:00 AM", "콘서트 설명 9"),
        new EventDTO(40L, "콘서트10", "콘서트", "2025-03-02", "2025-03-04", 127.0300, 37.4975, 160, "진행중", "11:00 PM - 02:00 AM", "콘서트 설명 10")
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

    // 팝업스토어 상세 조회
    @Operation(summary = "팝업스토어 상세 조회", description = "주어진 팝업스토어의 상세 정보를 조회합니다.")
    @GetMapping("/popup/{eventId}")
    public ResponseEntity<EventDTO> getPopupStoreDetail(@PathVariable Long eventId) {
        // 첫 번째 팝업스토어 이벤트 반환
        EventDTO event = new EventDTO(1L, "팝업스토어1", "팝업스토어", "2025-02-15", "2025-02-17", 127.0276, 37.4979, 10, "진행중", "10:00 AM - 08:00 PM", "팝업스토어 설명");
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    // 전시회 상세 조회
    @Operation(summary = "전시회 상세 조회", description = "주어진 전시회의 상세 정보를 조회합니다.")
    @GetMapping("/exhibition/{eventId}")
    public ResponseEntity<EventDTO> getExhibitionDetail(@PathVariable Long eventId) {
        // 첫 번째 전시회 이벤트 반환
        EventDTO event = new EventDTO(11L, "전시회1", "전시회", "2025-02-16", "2025-02-18", 127.0300, 37.4990, 20, "오픈예정", "09:00 AM - 06:00 PM", "전시회 설명");
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    // 뮤지컬 상세 조회
    @Operation(summary = "뮤지컬 상세 조회", description = "주어진 뮤지컬의 상세 정보를 조회합니다.")
    @GetMapping("/musical/{eventId}")
    public ResponseEntity<EventDTO> getMusicalDetail(@PathVariable Long eventId) {
        // 첫 번째 뮤지컬 이벤트 반환
        EventDTO event = new EventDTO(21L, "뮤지컬1", "뮤지컬", "2025-02-18", "2025-02-20", 127.0320, 37.4950, 30, "진행중", "12:00 PM - 10:00 PM", "뮤지컬 설명");
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    // 콘서트 상세 조회
    @Operation(summary = "콘서트 상세 조회", description = "주어진 콘서트의 상세 정보를 조회합니다.")
    @GetMapping("/concert/{eventId}")
    public ResponseEntity<EventDTO> getConcertDetail(@PathVariable Long eventId) {
        // 첫 번째 콘서트 이벤트 반환
        EventDTO event = new EventDTO(31L, "콘서트1", "콘서트", "2025-02-20", "2025-02-22", 127.0290, 37.4980, 40, "진행종료", "08:00 PM - 11:00 PM", "콘서트 설명");
        return ResponseEntity.status(HttpStatus.OK).body(event);
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
            description = "상태별(진행중, 오픈예정) , 지역별 정렬합니다.")
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
