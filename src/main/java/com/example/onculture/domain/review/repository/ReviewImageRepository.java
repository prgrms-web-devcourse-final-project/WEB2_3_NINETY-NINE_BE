package com.example.onculture.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.onculture.domain.review.domain.Review;
import com.example.onculture.domain.review.domain.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
	void deleteAllByReview(Review review);

}
