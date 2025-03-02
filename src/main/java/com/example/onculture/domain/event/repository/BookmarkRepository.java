package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserIdAndPerformanceId(Long userId, @Param("eventPostId") Long performanceId);

    Optional<Bookmark> findByUserIdAndExhibitEntitySeq(Long userId, @Param("eventPostId") Long exhibitEntitySeq);

    Optional<Bookmark> findByUserIdAndFestivalPostId(Long userId, @Param("eventPostId") Long festivalPostId);

    Optional<Bookmark> findByUserIdAndPopupStorePostId(Long userId,  @Param("eventPostId")Long popupStorePostId);
}

