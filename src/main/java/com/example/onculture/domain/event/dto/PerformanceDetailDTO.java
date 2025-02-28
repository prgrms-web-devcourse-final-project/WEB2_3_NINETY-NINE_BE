package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.Musical;
import com.example.onculture.domain.event.domain.Theater;
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

    // 공연 관련 정보
    @JacksonXmlProperty(localName = "prfcast")
    private String cast;

    @JacksonXmlProperty(localName = "prfcrew")
    private String crew;

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

    @JacksonXmlProperty(localName = "openrun")
    private String openRun;

    @JacksonXmlProperty(localName = "child")
    private String childPolicy;

    @JacksonXmlProperty(localName = "daehakro")
    private String daehakro;

    @JacksonXmlProperty(localName = "festival")
    private String festivalFlag;

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

    // 제작사 관련 정보
    @JacksonXmlProperty(localName = "entrpsnmP")
    private String productionCompany;

    @JacksonXmlProperty(localName = "entrpsnmA")
    private String planningCompany;

    @JacksonXmlProperty(localName = "entrpsnmH")
    private String presenter;

    @JacksonXmlProperty(localName = "entrpsnmS")
    private String organizer;

    public Musical convertToMusical() {
        Musical musical = new Musical();
        musical.setPerformanceId(this.performanceId);
        musical.setFacilityId(this.facilityId != null ? this.facilityId : "UNKNOWN_CATEGORY_ID");
        musical.setPerformanceTitle(this.performanceTitle);
        musical.setStartDate(this.startDate);
        musical.setEndDate(this.endDate);
        musical.setFacilityName(this.facilityName);
        musical.setCast(this.cast);
        musical.setCrew(this.crew);
        musical.setRuntime(this.runtime);
        musical.setAgeRating(this.ageRating);
        musical.setTicketPrice(this.ticketPrice);
        musical.setPosterUrl(this.posterUrl);
        musical.setIntroduction(this.introduction);
        musical.setArea(this.area);
        musical.setGenre(this.genre);
        musical.setOpenRun(this.openRun);
        musical.setChildPolicy(this.childPolicy);
        musical.setDaehakro(this.daehakro);
        musical.setFestivalFlag(this.festivalFlag);
        musical.setUpdateDate(this.updateDate);
        musical.setPerformanceState(this.performanceState);
        musical.setShowTimes(this.showTimes);
        musical.setProductionCompany(this.productionCompany);
        musical.setPlanningCompany(this.planningCompany);
        musical.setPresenter(this.presenter);
        musical.setOrganizer(this.organizer);

        // 여러 개의 스타일 URL을 하나의 문자열로 변환
        if (this.styleUrls != null && !this.styleUrls.isEmpty()) {
            musical.setStyleUrls(String.join(",", this.styleUrls));
        }

        // 여러 개의 관련 링크를 하나의 문자열로 변환
        if (this.relatedLinks != null && !this.relatedLinks.isEmpty()) {
            musical.setRelatedLinks(
                    this.relatedLinks.stream()
                            .map(link -> link.getRelatename() + " - " + link.getRelateurl())
                            .collect(Collectors.joining(", "))
            );
        }

        return musical;
    }

    public Theater convertToTheater() {
        Theater Theater = new Theater();
        Theater.setPerformanceId(this.performanceId);
        Theater.setFacilityId(this.facilityId != null ? this.facilityId : "UNKNOWN_CATEGORY_ID");
        Theater.setPerformanceTitle(this.performanceTitle);
        Theater.setStartDate(this.startDate);
        Theater.setEndDate(this.endDate);
        Theater.setFacilityName(this.facilityName);
        Theater.setCast(this.cast);
        Theater.setCrew(this.crew);
        Theater.setRuntime(this.runtime);
        Theater.setAgeRating(this.ageRating);
        Theater.setTicketPrice(this.ticketPrice);
        Theater.setPosterUrl(this.posterUrl);
        Theater.setIntroduction(this.introduction);
        Theater.setArea(this.area);
        Theater.setGenre(this.genre);
        Theater.setOpenRun(this.openRun);
        Theater.setChildPolicy(this.childPolicy);
        Theater.setDaehakro(this.daehakro);
        Theater.setFestivalFlag(this.festivalFlag);
        Theater.setUpdateDate(this.updateDate);
        Theater.setPerformanceState(this.performanceState);
        Theater.setShowTimes(this.showTimes);
        Theater.setProductionCompany(this.productionCompany);
        Theater.setPlanningCompany(this.planningCompany);
        Theater.setPresenter(this.presenter);
        Theater.setOrganizer(this.organizer);

        // 여러 개의 스타일 URL을 하나의 문자열로 변환
        if (this.styleUrls != null && !this.styleUrls.isEmpty()) {
            Theater.setStyleUrls(String.join(",", this.styleUrls));
        }

        // 여러 개의 관련 링크를 하나의 문자열로 변환
        if (this.relatedLinks != null && !this.relatedLinks.isEmpty()) {
            Theater.setRelatedLinks(
                    this.relatedLinks.stream()
                            .map(link -> link.getRelatename() + " - " + link.getRelateurl())
                            .collect(Collectors.joining(", "))
            );
        }

        return Theater;
    }
}

@Data
class RelatedLink {
    @JacksonXmlProperty(localName = "relatenm")
    private String relatename;

    @JacksonXmlProperty(localName = "relateurl")
    private String relateurl;
}
