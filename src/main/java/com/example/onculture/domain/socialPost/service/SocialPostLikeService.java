package com.example.onculture.domain.socialPost.service;

import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.repository.NotificationRepository;
import com.example.onculture.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    public String toggleLike(Long userId, Long socialPostId) {
        User user = findUserOrThrow(userId);

        SocialPost socialPost = findSocialPostOrThrow(socialPostId);

        Optional<SocialPostLike> existingLike = socialPostLikeRepository.findByUserIdAndSocialPostId(userId, socialPostId);

        if (existingLike.isPresent()) {
            socialPostLikeRepository.delete(existingLike.get());  // 이미 존재하면 좋아요 취소

            socialPost.decreaseLikeCount();

            socialPostRepository.save(socialPost);

            return "좋아요 취소";
        } else {
            SocialPostLike newLike = new SocialPostLike(user, socialPost);
            socialPostLikeRepository.save(newLike);  // 존재하지 않으면 좋아요 추가

            socialPost.increaseLikeCount();

            socialPostRepository.save(socialPost);

            // 게시글 작성자에게 알림 보내기 (자기 자신의 게시글이면 알림 못 가게끔)
            Long postOwnerId = socialPost.getUser().getId();
            if (!postOwnerId.equals(userId)) {  // 자기 자신의 게시글에 대한 좋아요는 알림 안 보냄
                NotificationRequestDTO notificationDTO = new NotificationRequestDTO(
                    postOwnerId,           // 알림 받을 사용자 ID (게시글 작성자)
                    userId,                // 알림 보낸 사용자 ID (좋아요 누른 사람)
                    Notification.NotificationType.LIKE,
                    "회원님 게시글에 새로운 좋아요가 추가되었습니다.",
                    socialPostId,
                    Notification.RelatedType.POST
                );
                notificationService.createNotification(notificationDTO);
            }

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
