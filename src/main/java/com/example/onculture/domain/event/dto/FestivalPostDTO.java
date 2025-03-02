package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.FestivalPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalPostDTO {
    private Long id;
    private String festivalPostUrl;
    private String festivalContent;
    private Date festivalStartDate;
    private Date festivalEndDate;
    private String festivalLocation;
    private String festivalDetails;
    private String festivalTicketPrice;
    private String festivalStatus;
    private List<String> imageUrls;

    // 엔티티를 받아 DTO로 변환하는 생성자 추가
    public FestivalPostDTO(FestivalPost festivalPost) {
        this.id = festivalPost.getId();
        this.festivalPostUrl = festivalPost.getFestivalPostUrl();
        this.festivalContent = festivalPost.getFestivalContent();
        this.festivalStartDate = festivalPost.getFestivalStartDate();
        this.festivalEndDate = festivalPost.getFestivalEndDate();
        this.festivalLocation = festivalPost.getFestivalLocation();
        this.festivalDetails = festivalPost.getFestivalDetails();
        this.festivalTicketPrice = festivalPost.getFestivalTicketPrice();
        this.festivalStatus = festivalPost.getFestivalStatus();
        this.imageUrls = festivalPost.getImageUrls();
    }
}
