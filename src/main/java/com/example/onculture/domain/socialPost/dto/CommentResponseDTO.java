package com.example.onculture.domain.socialPost.dto;

import com.example.onculture.domain.socialPost.domain.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDTO {
    private Long id;
    private Long socialPostId;
    private Long userId;
    private String content;
    private String userNickname;
    private String userProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.socialPostId = comment.getSocialPost().getId();
        this.userId = comment.getUser().getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.userNickname = comment.getUser().getProfile().getUser().getNickname();
        this.userProfileImage = comment.getUser().getProfile().getProfileImage();
    }
}

