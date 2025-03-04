package com.example.onculture.domain.event.domain;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibit_entity_id")
    private ExhibitEntity exhibitEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_post_id")
    private FestivalPost festivalPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_Store_post_id")
    private PopupStorePost popupStorePost;

    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void validateSingleAssociation() {
        int count = 0;
        if (performance != null) count++;
        if (exhibitEntity != null) count++;
        if (festivalPost != null) count++;
        if (popupStorePost != null) count++;
        if (count != 1) {
            throw new CustomException(ErrorCode.INVALID_BOOKMARK_REQUEST);
        }
    }
}
