package com.example.onculture.domain.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicDataRequestDTO {
    private String title;
    private String startDate;
    private String endDate;
    private String place;
    private String realmName;
    private String area;
    private String thumbnail;
    private Double gpsX;
    private Double gpsY;

}
