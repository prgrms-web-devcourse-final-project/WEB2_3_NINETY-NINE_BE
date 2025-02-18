package com.example.onculture.domain.socialPost.dto;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String imageUrl;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDTO(SocialPost socialPost) {
        this.id = socialPost.getId();
        this.userId = socialPost.getUserId();
        this.title = socialPost.getTitle();
        this.content = socialPost.getContent();
        this.imageUrl = socialPost.getImageUrl();
        this.likeCount = socialPost.getLikeCount();
        this.viewCount = socialPost.getViewCount();
        this.commentCount = socialPost.getCommentCount();
        this.createdAt = socialPost.getCreatedAt();
        this.updatedAt = socialPost.getUpdatedAt();
    }
}
