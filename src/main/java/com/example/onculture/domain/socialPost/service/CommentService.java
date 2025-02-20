package com.example.onculture.domain.socialPost.service;

import com.example.onculture.domain.socialPost.domain.Comment;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.CommentListResponseDTO;
import com.example.onculture.domain.socialPost.dto.CommentResponseDTO;
import com.example.onculture.domain.socialPost.dto.CreateCommentRequestDTO;
import com.example.onculture.domain.socialPost.dto.UpdateCommentRequestDTO;
import com.example.onculture.domain.socialPost.repository.CommentRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
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

    public CommentListResponseDTO getCommentsByPost(int pageNum, int pageSize, Long socialPostId) {
        existsBySocialPostId(socialPostId);

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
        existsByUserId(userId);

        existsBySocialPostId(socialPostId);

        Comment comment = Comment.builder()
                .socialPostId(socialPostId)
                .userId(userId)
                .content(requestDTO.getContent())
                .build();

        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public CommentResponseDTO updateCommentByPost(Long userId,
                                                  Long socialPostId,
                                                  Long commentId,
                                                  UpdateCommentRequestDTO requestDTO) {
        existsByUserId(userId);

        validateOwner(commentId, userId);

        existsBySocialPostId(socialPostId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (isNotCommentFromPost(comment, socialPostId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        comment.updateComment(requestDTO);

        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public String deleteCommentByPost(Long userId, Long socialPostId, Long commentId) {
        existsByUserId(userId);

        validateOwner(commentId, userId);

        existsBySocialPostId(socialPostId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (isNotCommentFromPost(comment, socialPostId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        commentRepository.deleteById(commentId);

        return "삭제 완료";
    }

    private void existsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void existsBySocialPostId(Long socialPostId) {
        if (!socialPostRepository.existsById(socialPostId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private void validatePageInput(int pageNum, int pageSize) {
        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    public void validateOwner(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_MANAGE);
        }
    }

    public boolean isNotCommentFromPost(Comment comment, Long socialPostId) {
        return !comment.getSocialPostId().equals(socialPostId);
    }
}
