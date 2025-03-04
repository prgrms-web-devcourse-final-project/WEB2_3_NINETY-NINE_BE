package com.example.onculture.domain.event.domain;

import com.example.onculture.domain.event.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
@Setter
@Table(name = "popup_store_post")
public class PopupStorePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_url", nullable = false)
    private String postUrl;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "popups_start_date", length = 2000)
    private Date popupsStartDate;

    @Column(name = "popups_end_date")
    private Date popupsEndDate; // 종료 일자 추가

    @Column(name = "operating_time", length = 50)
    private String operatingTime;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "status")
    private String status;

    @Column(name = "popups_area")
    private String popupsArea;

    // 기존의 이미지 URL 목록 매핑 대신, 단일 컬럼에 저장 (쉼표 구분 문자열)
    @Convert(converter = StringListConverter.class)
    @Column(name = "image_urls", columnDefinition = "LONGTEXT")
    private List<String> imageUrls;

    @OneToMany(mappedBy = "popupStorePost", cascade = CascadeType.ALL)
    private List<Bookmark> bookmark = new ArrayList<>();

}
