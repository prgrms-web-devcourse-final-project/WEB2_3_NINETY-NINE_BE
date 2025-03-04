package com.example.onculture.domain.socialPost.service;

import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.service.NotificationService;
import com.example.onculture.domain.socialPost.domain.Comment;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.CommentListResponseDTO;
import com.example.onculture.domain.socialPost.dto.CommentResponseDTO;
import com.example.onculture.domain.socialPost.dto.CreateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import com.example.onculture.domain.socialPost.repository.CommentRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final SocialPostRepository socialPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentListResponseDTO getCommentsByPost(int pageNum, int pageSize, Long socialPostId) {
        findSocialPostOrThrow(socialPostId);

        validatePageInput(pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());

        Page<CommentResponseDTO> comments = commentRepository
                .findBySocialPostId(socialPostId, pageable)
                .map(CommentResponseDTO::new);

        return CommentListResponseDTO.builder()
                .comments(comments.getContent())
                .totalPages(comments.getTotalPages())
                .pageNum(comments.getNumber())
                .pageSize(comments.getSize())
                .totalElements(comments.getTotalElements())
                .numberOfElements(comments.getNumberOfElements())
                .build();
    }

    public CommentResponseDTO createCommentByPost(Long userId,
                                                  Long socialPostId,
                                                  CreateCommentRequestDTO requestDTO) {
        User user = findUserOrThrow(userId);

        SocialPost socialPost = findSocialPostOrThrow(socialPostId);

        Comment comment = Comment.builder()
                .socialPost(socialPost)
                .user(user)
                .content(requestDTO.getContent())
                .build();

        commentRepository.save(comment);

        socialPost.increaseCommentCount();

        socialPostRepository.save(socialPost);

        // 게시글 작성자 ID 가져오기
        Long postOwnerId = socialPost.getUser().getId();

        // 알림 생성
        NotificationRequestDTO notificationDTO = new NotificationRequestDTO(
            postOwnerId,           // 알림 받을 사용자 ID (게시글 작성자)
            userId,                // 알림 보낸 사용자 ID (댓글 작성자)
            Notification.NotificationType.COMMENT,
            "회원님 게시글에 새로운 댓글이 달렸습니다.",
            socialPostId,
            Notification.RelatedType.COMMENT
        );
        notificationService.createNotification(notificationDTO);

        return new CommentResponseDTO(comment);
    }

    public CommentResponseDTO updateCommentByPost(Long userId,
                                                  Long socialPostId,
                                                  Long commentId,
                                                  UpdateCommentRequestDTO requestDTO) {
        User user = findUserOrThrow(userId);

        validateOwner(commentId, user);

        SocialPost socialPost = findSocialPostOrThrow(socialPostId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (isNotCommentFromPost(comment, socialPost)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        comment.updateComment(requestDTO);

        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public String deleteCommentByPost(Long userId, Long socialPostId, Long commentId) {
        User user = findUserOrThrow(userId);

        validateOwner(commentId, user);

        SocialPost socialPost = findSocialPostOrThrow(socialPostId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (isNotCommentFromPost(comment, socialPost)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        commentRepository.deleteById(commentId);

        socialPost.decreaseCommentCount();

        socialPostRepository.save(socialPost);

        return "삭제 완료";
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private SocialPost findSocialPostOrThrow(Long socialPostId) {
        return socialPostRepository.findById(socialPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePageInput(int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    public void validateOwner(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!(comment.getUser() == user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_MANAGE);
        }
    }

    public boolean isNotCommentFromPost(Comment comment, SocialPost socialPost) {
        return !(comment.getSocialPost() == socialPost);
    }
}
