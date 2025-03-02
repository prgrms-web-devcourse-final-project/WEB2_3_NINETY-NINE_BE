package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.PopupStorePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PopupStorePostRepository extends JpaRepository<PopupStorePost, Long> {

    @Query(value = "SELECT * FROM popup_store_post WHERE status = '진행중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<PopupStorePost> findRandomPopupStorePosts(int randomSize);

    // 제목(title)를 포함하는 게시글을 검색
    List<PopupStorePost> findByContentContaining(String title);
}