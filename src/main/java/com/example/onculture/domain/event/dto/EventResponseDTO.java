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

    public boolean getBookmarked() {
        return this.isBookmarked;
    }
    public EventResponseDTO(ExhibitEntity exhibitEntity, boolean isBookmarked) {
        this.id = exhibitEntity.getSeq();
        this.genre = exhibitEntity.getArea();
        this.postUrl = exhibitEntity.getThumbnail();
        this.ageRating = null;
        this.title = exhibitEntity.getTitle();
        this.startDate = exhibitEntity.getStartDate();
        this.endDate = exhibitEntity.getEndDate();
        this.operatingHours = null;
        this.location = exhibitEntity.getArea();
        this.venue = exhibitEntity.getPlace();
        this.status = exhibitEntity.getExhibitStatus();
        this.ticketingWebSite = null;
        this.price = null;
        this.detailImage = null;
        this.description = null;
        this.isBookmarked = isBookmarked;
    }

    public EventResponseDTO(PopupStorePost popupStorePost, boolean isBookmarked) {
        this.id = popupStorePost.getId();
        this.genre = null;
        this.postUrl = popupStorePost.getImageUrls().toString();
        this.ageRating = null;
        this.title = popupStorePost.getContent();
        this.startDate = String.valueOf(popupStorePost.getPopupsStartDate());
        this.endDate = String.valueOf(popupStorePost.getPopupsEndDate());
        this.operatingHours = null;
        this.location = popupStorePost.getPopupsArea();
        this.venue = popupStorePost.getLocation();
        this.status = popupStorePost.getStatus();
        this.ticketingWebSite = null;
        this.price = null;
        this.detailImage = null;
        this.description = popupStorePost.getDetails();
        this.isBookmarked = isBookmarked;
    }

    public EventResponseDTO(FestivalPost festivalPost, boolean isBookmarked) {
        this.id = festivalPost.getId();
        this.genre = null;
        this.postUrl = festivalPost.getImageUrls().toString();
        this.ageRating = null;
        this.title = festivalPost.getFestivalContent();
        this.startDate = String.valueOf(festivalPost.getFestivalStartDate());
        this.endDate = String.valueOf(festivalPost.getFestivalEndDate());
        this.operatingHours = null;
        this.location = festivalPost.getFestivalArea();
        this.venue = festivalPost.getFestivalLocation();
        this.status = festivalPost.getFestivalStatus();
        this.ticketingWebSite = null;
        this.price = null;
        this.detailImage = null;
        this.description = festivalPost.getFestivalContent();
        this.isBookmarked = isBookmarked;
    }

}
