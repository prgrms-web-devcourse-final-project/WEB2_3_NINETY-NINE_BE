package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExhibitDTO {
    private Long seq;
    private String title;
    private Date startDate;
    private Date endDate;
    private String place;
    private String realmName;
    private String area;
    private String thumbnail;
    private Double gpsX;
    private Double gpsY;
    private String exhibitStatus; //전시회 상태 추가
}
