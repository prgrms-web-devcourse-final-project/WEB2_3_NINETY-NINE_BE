package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.Performance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String genre;
    private String postUrl;
    private String ageRating;
    private String title;
    private String startDate;
    private String endDate;
    private String operatingHours;
    private String location;
    private String venue;
    private String status;
    private String ticketingWebSite;
    private String price;
    private String detailImage;
    private String description;

    public EventResponseDTO(Performance performance) {
        this.id = performance.getId();
        this.genre = performance.getGenre();
        this.postUrl = performance.getPosterUrl();
        this.ageRating = performance.getAgeRating();
        this.title = performance.getPerformanceTitle();
        this.startDate = performance.getStartDate();
        this.endDate = performance.getEndDate();
        this.operatingHours = performance.getShowTimes();
        this.location = performance.getArea();
        this.venue = performance.getFacilityName();
        this.status = performance.getPerformanceState();
        this.ticketingWebSite = performance.getRelatedLinks();
        this.price = performance.getTicketPrice();
        this.detailImage = performance.getStyleUrls();
        this.description = null;
    }
}
