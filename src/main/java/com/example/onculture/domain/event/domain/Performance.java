package com.example.onculture.domain.event.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Performance {

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
    private String runtime;
    private String ageRating;
    private String ticketPrice;
    private String posterUrl;
    private String area;
    private String genre;
    private String updateDate;
    private String performanceState;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "show_times", length = 500)
    private String showTimes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String styleUrls;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String relatedLinks;
}
