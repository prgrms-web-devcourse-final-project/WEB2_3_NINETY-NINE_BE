package com.example.onculture.domain.socialPost.repository;

import com.example.onculture.domain.socialPost.domain.SocialPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialPostLikeRepository extends JpaRepository<SocialPostLike, Long> {
    Optional<SocialPostLike> findByUserIdAndSocialPostId(Long userId, Long socialPostId);
}
