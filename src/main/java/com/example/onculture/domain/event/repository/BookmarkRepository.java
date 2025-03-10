package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Page<Bookmark> findAllByUserId(Long userId, Pageable pageable);

    Optional<Bookmark> findByUserIdAndPerformanceId(Long userId, @Param("eventPostId") Long performanceId);

    Optional<Bookmark> findByUserIdAndExhibitEntitySeq(Long userId, @Param("eventPostId") Long exhibitEntitySeq);

    Optional<Bookmark> findByUserIdAndFestivalPostId(Long userId, @Param("eventPostId") Long festivalPostId);

    Optional<Bookmark> findByUserIdAndPopupStorePostId(Long userId,  @Param("eventPostId")Long popupStorePostId);

    // 북마크한 게시글 중 오늘 오픈하는 게시글 조회
    @Query("""
        SELECT b FROM Bookmark b 
        WHERE 
            (b.performance.startDate = CURRENT_DATE AND b.performance IS NOT NULL) OR
            (b.exhibitEntity.startDate = CURRENT_DATE AND b.exhibitEntity IS NOT NULL) OR
            (b.festivalPost.festivalStartDate = CURRENT_DATE AND b.festivalPost IS NOT NULL) OR
            (b.popupStorePost.popupsStartDate = CURRENT_DATE AND b.popupStorePost IS NOT NULL)
    """)
    List<Bookmark> findBookmarksWithOpeningToday();

    // 북마크한 게시글 중 오늘 마감하는 게시글 조회
    @Query("""
        SELECT b FROM Bookmark b 
        WHERE 
            (b.performance.endDate = CURRENT_DATE AND b.performance IS NOT NULL) OR
            (b.exhibitEntity.endDate = CURRENT_DATE AND b.exhibitEntity IS NOT NULL) OR
            (b.festivalPost.festivalEndDate = CURRENT_DATE AND b.festivalPost IS NOT NULL) OR
            (b.popupStorePost.popupsEndDate = CURRENT_DATE AND b.popupStorePost IS NOT NULL)
    """)
    List<Bookmark> findBookmarksWithClosingToday();
}

