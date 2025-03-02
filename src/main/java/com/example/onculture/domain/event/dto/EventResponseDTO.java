package com.example.onculture.domain.event.dto;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.domain.PopupStorePost;
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
    private boolean isBookmarked;

    public EventResponseDTO(Performance performance, boolean isBookmarked) {
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
        this.isBookmarked = isBookmarked;
    }

    public EventResponseDTO(ExhibitEntity exhibit, boolean isBookmarked) {
        this.id = exhibit.getSeq();
        this.genre = exhibit.getRealmName();
        this.postUrl = exhibit.getThumbnail();
        this.ageRating = null;
        this.title = exhibit.getTitle();
        this.startDate = exhibit.getStartDate();
        this.endDate = exhibit.getEndDate();
        this.operatingHours = null;
        this.location = exhibit.getPlace();
        this.venue = exhibit.getPlace();
        this.status = null;
        this.ticketingWebSite = null;
        this.price = null;
        this.detailImage = exhibit.getThumbnail();
        this.description = null;
        this.isBookmarked = isBookmarked;
    }

    public EventResponseDTO(FestivalPost festival, boolean isBookmarked) {
        this.id = festival.getId();
        this.genre = "festival";
        this.postUrl = festival.getFestivalPostUrl();
        this.ageRating = null;
        this.title = festival.getFestivalContent();
        this.startDate = festival.getFestivalStartDate() != null
                ? festival.getFestivalStartDate().toString() : null;
        this.endDate = festival.getFestivalEndDate() != null
                ? festival.getFestivalEndDate().toString() : null;
        this.operatingHours = null;
        this.location = festival.getFestivalLocation();
        this.venue = null;
        this.status = festival.getFestivalStatus();
        this.ticketingWebSite = null;
        this.price = festival.getFestivalTicketPrice();
        this.detailImage = null;
        this.description = festival.getFestivalDetails();
        this.isBookmarked = isBookmarked;
    }

    public EventResponseDTO(PopupStorePost popup, boolean isBookmarked) {
        this.id = popup.getId();
        this.genre = "popupStore";
        this.postUrl = popup.getPostUrl();
        this.ageRating = null;
        this.title = popup.getContent();
        this.startDate = popup.getOperatingDate() != null
                ? popup.getOperatingDate().toString() : null;
        this.endDate = popup.getPopupsEndDate() != null
                ? popup.getPopupsEndDate().toString() : null;
        this.operatingHours = popup.getOperatingTime();
        this.location = popup.getLocation();
        this.venue = null;
        this.status = popup.getStatus();
        this.ticketingWebSite = null;
        this.price = null;
        this.detailImage = null;
        this.description = popup.getDetails();
        this.isBookmarked = isBookmarked;
    }
}
