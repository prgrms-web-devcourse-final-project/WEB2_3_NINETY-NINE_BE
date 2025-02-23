package com.example.onculture.domain.event.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.sql.Date;

@Entity
@Data
@Table(name = "popup_store_post")
public class PopupStorePost {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String postUrl;

    @Column(length = 2000)
    private String content;

    private Date operatingDate;

    private String operatingTime;

    private String location;

    @Column(length = 2000)
    private String details;

}