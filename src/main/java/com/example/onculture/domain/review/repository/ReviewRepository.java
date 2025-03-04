package com.example.onculture.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.onculture.domain.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByExhibitSeq(Long exhibitionId);
	List<Review> findByFestivalId(Long festivalId);
	List<Review> findByPerformanceId(Long performanceId);
	List<Review> findByPopupStoreId(Long popupStoreId);

}
