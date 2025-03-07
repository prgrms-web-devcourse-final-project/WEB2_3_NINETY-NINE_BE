package com.example.onculture.domain.socialPost.repository;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.domain.SocialPostLike;
import com.example.onculture.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SocialPostLikeRepository extends JpaRepository<SocialPostLike, Long> {
    Optional<SocialPostLike> findByUserIdAndSocialPostId(Long userId, Long socialPostId);
    List<Long> findSocialPostIdByUserId(Long userId);
    boolean existsByUserAndSocialPost(User user, SocialPost socialPost);
    boolean existsByUserIdAndSocialPostId(Long userId, Long socialPostId);

    @Query("select spl.socialPost.id from SocialPostLike spl where spl.user.id = :userId and spl.socialPost.id in :socialPostIds")
    List<Long> findSocialPostIdsByUserIdAndSocialPostIds(Long userId, List<Long> socialPostIds);

}
