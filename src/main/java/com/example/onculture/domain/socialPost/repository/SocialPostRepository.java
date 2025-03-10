package com.example.onculture.domain.socialPost.repository;


import com.example.onculture.domain.socialPost.domain.SocialPostLike;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.onculture.domain.socialPost.domain.SocialPost;

import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {
    Page<SocialPost> findByUserId(Long userId, Pageable pageable);

    // n + 1 문제 해결을 위한 패치 조인 쿼리
    @Query(value = "select sp from SocialPost sp " +
            "join fetch sp.user u " +
            "left join fetch u.profile",
            countQuery = "select count(sp) from SocialPost sp")
    Page<SocialPost> findAllWithUserAndProfile(Pageable pageable);
}



