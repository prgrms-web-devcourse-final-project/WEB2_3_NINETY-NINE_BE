package com.example.onculture.domain.event.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "exhibit")
public class ExhibitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq; // 공연/전시 일련번호

    private String title;       // 제목
    private Date startDate;
    private Date endDate;
    private String place;       // 공연 장소
    private String realmName;   // 분야명 (예: 연극, 콘서트, 전시 등)
    private String area;        // 지역 (예: 서울)
    private String thumbnail;   // 썸네일 URL/ 포스터
    private Double gpsX;        // 경도
    private Double gpsY;        // 위도
    private String exhibitStatus; //전시회 상태 추가

    @OneToMany(mappedBy = "exhibitEntity", cascade = CascadeType.ALL)
    private List<Bookmark> bookmark = new ArrayList<>();
}
