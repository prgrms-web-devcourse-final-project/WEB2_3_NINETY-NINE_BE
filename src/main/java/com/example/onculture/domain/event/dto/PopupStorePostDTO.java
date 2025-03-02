package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.PopupStorePost;
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
public class PopupStorePostDTO {
    private Long id;
    private String postUrl;
    private String content;
    private Date popupsStartDate;
    private Date popupsEndDate;
    private String operatingTime;
    private String location;
    private String details;
    private String status;
    private List<String> imageUrls;

    // 엔티티를 받아 DTO로 변환하는 생성자 추가
    public PopupStorePostDTO(PopupStorePost post) {
        this.id = post.getId();
        this.postUrl = post.getPostUrl();
        this.content = post.getContent();
        this.popupsStartDate = post.getPopupsStartDate();
        this.popupsEndDate = post.getPopupsEndDate();
        this.operatingTime = post.getOperatingTime();
        this.location = post.getLocation();
        this.details = post.getDetails();
        this.status = post.getStatus();
        this.imageUrls = post.getImageUrls();
    }

}
