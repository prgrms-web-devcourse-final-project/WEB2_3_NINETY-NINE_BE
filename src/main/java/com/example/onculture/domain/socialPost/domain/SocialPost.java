package com.example.onculture.domain.socialPost.domain;

import com.example.onculture.domain.socialPost.dto.UpdatePostRequestDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SocialPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @CreationTimestamp
    @Column(nullable = false)

    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSocialPost(UpdatePostRequestDTO requestDTO) {
        this.title = requestDTO.getTitle();
        this.content = requestDTO.getContent();
        this.imageUrl = requestDTO.getImageUrl();
    }
}
