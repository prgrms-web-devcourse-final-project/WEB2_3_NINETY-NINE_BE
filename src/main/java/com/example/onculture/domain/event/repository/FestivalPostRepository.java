package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.FestivalPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FestivalPostRepository extends JpaRepository<FestivalPost, Long> {
    // 제목(title)를 포함하는 게시글을 검색
    List<FestivalPost> findByFestivalContentContaining(String title);

    @Query(value = "SELECT * FROM festival_post WHERE festival_status = '진행중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<FestivalPost> findRandomFestivalPosts(int randomSize);

    // festivalLocation 필드가 null이 아닌 항목들만 조회 (필요에 따라)
    List<FestivalPost> findByFestivalLocationIsNotNull();
}