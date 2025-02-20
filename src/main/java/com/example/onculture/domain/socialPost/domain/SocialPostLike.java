package com.example.onculture.domain.socialPost.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SocialPostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long socialPostId;

    @Column(nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SocialPostLike(Long userId, Long socialPostId) {
        this.userId = userId;
        this.socialPostId = socialPostId;
    }
}
