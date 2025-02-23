package com.example.onculture.domain.event.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String performanceId;

    @Column(nullable = false)
    private String facilityId;

    @Column(nullable = false)
    private String performanceTitle;

    private String startDate;
    private String endDate;
    private String facilityName;
    private String cast;
    private String crew;
    private String runtime;
    private String ageRating;
    private String ticketPrice;
    private String posterUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;

    private String area;
    private String genre;
    private String openRun;
    private String childPolicy;
    private String daehakro;
    private String festivalFlag;
    private String updateDate;
    private String performanceState;
    private String showTimes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String styleUrls;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String relatedLinks;

    private String productionCompany;
    private String planningCompany;
    private String presenter;
    private String organizer;
}
