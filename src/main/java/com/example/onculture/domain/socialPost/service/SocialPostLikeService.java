package com.example.onculture.domain.socialPost.service;

import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.domain.SocialPostLike;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SocialPostLikeService {
    private final SocialPostLikeRepository socialPostLikeRepository;
    private final UserRepository userRepository;
    private final SocialPostRepository socialPostRepository;

    public String toggleLike(Long userId, Long socialPostId) {
        User user = findUserOrThrow(userId);

        SocialPost socialPost = findSocialPostOrThrow(socialPostId);

        Optional<SocialPostLike> existingLike = socialPostLikeRepository.findByUserIdAndSocialPostId(userId, socialPostId);

        if (existingLike.isPresent()) {
            socialPostLikeRepository.delete(existingLike.get());  // 이미 존재하면 좋아요 취소

            return "좋아요 취소";
        } else {
            SocialPostLike newLike = new SocialPostLike(user, socialPost);
            socialPostLikeRepository.save(newLike);  // 존재하지 않으면 좋아요 추가

            return "좋아요 추가";
        }
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private SocialPost findSocialPostOrThrow(Long socialPostId) {
        return socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
