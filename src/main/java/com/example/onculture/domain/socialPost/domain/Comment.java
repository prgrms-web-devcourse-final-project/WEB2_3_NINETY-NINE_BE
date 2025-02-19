package com.example.onculture.domain.socialPost.domain;

import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long socialPostId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
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
