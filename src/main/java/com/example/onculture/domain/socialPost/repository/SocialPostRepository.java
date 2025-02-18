package com.example.onculture.domain.socialPost.repository;


import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.onculture.domain.socialPost.domain.SocialPost;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {

    Page<SocialPost> findByUserId(Long userId, Pageable pageable);
}

