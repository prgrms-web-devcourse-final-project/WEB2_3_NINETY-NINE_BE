package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.PopupStorePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopupStorePostRepository extends JpaRepository<PopupStorePost, Long> {
    // 제목(title)를 포함하는 게시글을 검색
    List<PopupStorePost> findByContentContaining(String title);
}