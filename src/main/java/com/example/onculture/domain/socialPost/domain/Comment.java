package com.example.onculture.domain.socialPost.domain;

import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdatePostRequestDTO;
import com.example.onculture.domain.user.domain.User;
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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "social_post_id", nullable = false)
    private SocialPost socialPost;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateComment(UpdateCommentRequestDTO requestDTO) {
        this.content = requestDTO.getContent();
    }
}
