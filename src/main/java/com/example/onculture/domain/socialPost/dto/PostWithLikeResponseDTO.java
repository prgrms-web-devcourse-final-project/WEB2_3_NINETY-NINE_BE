package com.example.onculture.domain.socialPost.dto;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.global.utils.image.ImageUrlUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostWithLikeResponseDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private List<String> imageUrls;
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private String userNickname;
    private String userProfileImage;
    private boolean likeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostWithLikeResponseDTO(SocialPost socialPost, boolean likeStatus) {
        this.id = socialPost.getId();
        this.userId = socialPost.getUser().getId();
        this.title = socialPost.getTitle();
        this.content = socialPost.getContent();
        this.imageUrls = ImageUrlUtil.splitImageUrls(socialPost.getImageUrls());
        this.likeCount = socialPost.getLikeCount();
        this.viewCount = socialPost.getViewCount();
        this.commentCount = socialPost.getCommentCount();
        this.createdAt = socialPost.getCreatedAt();
        this.updatedAt = socialPost.getUpdatedAt();
        this.userNickname = socialPost.getUser().getNickname();
        this.userProfileImage = socialPost.getUser().getProfile().getProfileImage();
        this.likeStatus = likeStatus;
    }
}
