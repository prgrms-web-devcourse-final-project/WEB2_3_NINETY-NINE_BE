package com.example.onculture.domain.event.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "popup_store_post")
public class PopupStorePost {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_url", nullable = false)
    private String postUrl;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "operating_date", length = 2000)
    private Date operatingDate;

    @Column(name = "popups_end_date")
    private Date popupsEndDate; // 종료 일자 추가

    @Column(name = "operating_time", length = 50)
    private String operatingTime;


    @Column(name = "location",length = 255)
    private String location;

    @Column(name = "details",length = 2000)
    private String details;

    @Column(name = "status")
    private String status;

    // JPA 매핑: popup_store_post_images 테이블에 이미지 URL들을 저장
    @ElementCollection
    @CollectionTable(name = "popup_store_post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT", nullable = false)
    private List<String> imageUrls;

    @OneToMany(mappedBy = "popupStorePost", cascade = CascadeType.ALL)
    private List<Bookmark> bookmark = new ArrayList<>();
}
