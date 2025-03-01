package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.Performance;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceDetailDTO {

    // 기본 정보
    @JacksonXmlProperty(localName = "mt20id")
    private String performanceId;

    @JacksonXmlProperty(localName = "mt10id")
    private String facilityId;

    @JacksonXmlProperty(localName = "prfnm")
    private String performanceTitle;

    @JacksonXmlProperty(localName = "prfpdfrom")
    private String startDate;

    @JacksonXmlProperty(localName = "prfpdto")
    private String endDate;

    @JacksonXmlProperty(localName = "fcltynm")
    private String facilityName;

    @JacksonXmlProperty(localName = "prfruntime")
    private String runtime;

    @JacksonXmlProperty(localName = "prfage")
    private String ageRating;

    @JacksonXmlProperty(localName = "pcseguidance")
    private String ticketPrice;

    @JacksonXmlProperty(localName = "poster")
    private String posterUrl;

    @JacksonXmlProperty(localName = "sty")
    private String introduction;

    @JacksonXmlProperty(localName = "area")
    private String area;

    @JacksonXmlProperty(localName = "genrenm")
    private String genre;

    @JacksonXmlProperty(localName = "updatedate")
    private String updateDate;

    @JacksonXmlProperty(localName = "prfstate")
    private String performanceState;

    @JacksonXmlProperty(localName = "dtguidance")
    private String showTimes;

    // 공연 소개 이미지 URL 목록
    @JacksonXmlElementWrapper(localName = "styurls", useWrapping = true)
    @JacksonXmlProperty(localName = "styurl")
    private List<String> styleUrls;

    // 여러 개의 관련 링크 목록
    @JacksonXmlElementWrapper(localName = "relates", useWrapping = true)
    @JacksonXmlProperty(localName = "relate")
    private List<RelatedLink> relatedLinks;


    public Performance convertToPerformance() {
        Performance performance = new Performance();
        performance.setPerformanceId(this.performanceId);
        performance.setFacilityId(this.facilityId != null ? this.facilityId : "UNKNOWN_CATEGORY_ID");
        performance.setPerformanceTitle(this.performanceTitle);
        performance.setStartDate(this.startDate);
        performance.setEndDate(this.endDate);
        performance.setFacilityName(this.facilityName);
        performance.setRuntime(this.runtime);
        performance.setAgeRating(this.ageRating);
        performance.setTicketPrice(this.ticketPrice);
        performance.setPosterUrl(this.posterUrl);
        performance.setIntroduction(this.introduction);
        performance.setArea(this.area);
        performance.setGenre(this.genre);
        performance.setUpdateDate(this.updateDate);
        performance.setPerformanceState(this.performanceState);
        performance.setShowTimes(this.showTimes);

        // 여러 개의 스타일 URL을 하나의 문자열로 변환
        if (this.styleUrls != null && !this.styleUrls.isEmpty()) {
            performance.setStyleUrls(String.join(",", this.styleUrls));
        }

        // 여러 개의 관련 링크를 하나의 문자열로 변환
        if (this.relatedLinks != null && !this.relatedLinks.isEmpty()) {
            performance.setRelatedLinks(
                    this.relatedLinks.stream()
                            .map(link -> link.getRelatename() + " - " + link.getRelateurl())
                            .collect(Collectors.joining(", "))
            );
        }

        return performance;
    }
}

@Data
class RelatedLink {
    @JacksonXmlProperty(localName = "relatenm")
    private String relatename;

    @JacksonXmlProperty(localName = "relateurl")
    private String relateurl;
}
