package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExhibitDTO {
    private Long seq;
    private String title;
    private String startDate;
    private String endDate;
    private String place;
    private String realmName;
    private String area;
    private String thumbnail;
    private Double gpsX;
    private Double gpsY;
    private String exhibitStatus; //전시회 상태 추가

    // 엔티티를 받아 DTO로 변환하는 생성자 추가
    public ExhibitDTO(ExhibitEntity entity) {
        this.seq = entity.getSeq();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.place = entity.getPlace();
        this.realmName = entity.getRealmName();
        this.area = entity.getArea();
        this.thumbnail = entity.getThumbnail();
        this.gpsX = entity.getGpsX();
        this.gpsY = entity.getGpsY();
        this.exhibitStatus = entity.getExhibitStatus();
    }
}
