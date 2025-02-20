package com.example.onculture.domain.socialPost.repository;

import com.example.onculture.domain.socialPost.domain.Comment;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findBySocialPostId(Long socialPostId, Pageable pageable);
}
