package com.example.onculture.domain.socialPost.domain;

import com.example.onculture.domain.event.converter.StringListConverter;
import com.example.onculture.domain.socialPost.dto.UpdatePostRequestDTO;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.global.utils.image.ImageUrlUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SocialPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 수정
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT") // 추가 - 길이 제한 해제
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "socialPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "socialPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialPostLike> socialPostLikes = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // 수정
    public void updateSocialPost(UpdatePostRequestDTO requestDTO, List<String> newImageUrls) {
        this.title = requestDTO.getTitle();
        this.content = requestDTO.getContent();
        this.imageUrls = newImageUrls;
    }

    public void increaseViewCount() {
        this.viewCount++ ;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        this.commentCount--;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }
}
