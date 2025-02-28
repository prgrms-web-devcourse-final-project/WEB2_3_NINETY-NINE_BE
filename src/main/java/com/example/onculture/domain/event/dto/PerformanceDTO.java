package com.example.onculture.domain.event.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceDTO {

    @JacksonXmlProperty(localName = "mt20id")
    private String mt20id; // 공연 ID

    @JacksonXmlProperty(localName = "prfnm")
    private String prfnm; // 공연명

    @JacksonXmlProperty(localName = "prfpdfrom")
    private String prfpdfrom; // 공연 시작일

    @JacksonXmlProperty(localName = "prfpdto")
    private String prfpdto; // 공연 종료일

    @JacksonXmlProperty(localName = "fcltynm")
    private String fcltynm; // 공연장

    @JacksonXmlProperty(localName = "poster")
    private String poster; // 포스터 URL

    @JacksonXmlProperty(localName = "area")
    private String area; // 지역명

    @JacksonXmlProperty(localName = "genrenm")
    private String genrenm; // 장르명

    @JacksonXmlProperty(localName = "openrun")
    private String openrun; // 오픈런 여부

    @JacksonXmlProperty(localName = "prfstate")
    private String prfstate; // 공연 상태
}

