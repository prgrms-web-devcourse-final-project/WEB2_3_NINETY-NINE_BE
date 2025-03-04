package com.example.onculture.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventDTO {
    private Long event_id;              //공연 Id
    private String event_title;         //공연 이름
    private String category;            //공연 종류
    private String event_start_date;    //공연시작날짜
    private String event_end_date;      //공연종료날짜
    private double longitude;           //공연장소(경도)
    private double latitude;            //공연장소(위도)
    private int view_count;             //게시글 조회수
    private String event_status;        //공연상태
    private String operating_hours;     //운영시간
    private String event_description;   //공연설명

}