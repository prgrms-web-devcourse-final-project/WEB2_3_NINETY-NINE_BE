package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.FestivalPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalPostRepository extends JpaRepository<FestivalPost, Long> {
    // 위치(keyword)를 포함하는 게시글을 검색 (필드명 festivalLocation 사용)
    List<FestivalPost> findByFestivalLocationContaining(String keyword);
}