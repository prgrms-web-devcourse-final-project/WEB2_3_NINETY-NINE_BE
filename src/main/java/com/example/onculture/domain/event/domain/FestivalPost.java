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
@Table(name = "festival_post")
public class FestivalPost {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "festival_post_url")
    private String festivalPostUrl;

    @Column(name = "festival_content")
    private String festivalContent;

    @Column(name = "festival_start_date")
    private Date festivalStartDate;

    @Column(name = "festival_end_date")
    private Date festivalEndDate; // 종료 일자 추가

    @Column(name = "festival_location")
    private String festivalLocation;

    @Column(name = "festival_details", columnDefinition = "LONGTEXT")
    private String festivalDetails;

    @Column(name = "festival_ticket_price")
    private String festivalTicketPrice;

    @Column(name = "festival_status")
    private String festivalStatus;

    // JPA 매핑: festival_post_images 테이블에 이미지 URL들을 저장
    @ElementCollection
    @CollectionTable(name = "festival_post_images", joinColumns = @JoinColumn(name = "festival_post_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT", nullable = false)
    private List<String> imageUrls;

    @OneToMany(mappedBy = "festivalPost", cascade = CascadeType.ALL)
    private List<Bookmark> bookmark = new ArrayList<>();
}
