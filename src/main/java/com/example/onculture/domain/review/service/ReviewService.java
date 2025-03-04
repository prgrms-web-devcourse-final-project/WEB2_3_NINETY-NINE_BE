package com.example.onculture.domain.review.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.review.domain.Review;
import com.example.onculture.domain.review.domain.ReviewImage;
import com.example.onculture.domain.review.dto.ReviewRequestDTO;
import com.example.onculture.domain.review.dto.ReviewResponseDTO;
import com.example.onculture.domain.review.repository.ReviewRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ExhibitRepository exhibitRepository;
	private final FestivalPostRepository festivalPostRepository;
	private final PerformanceRepository performanceRepository;
	private final PopupStorePostRepository popupStorePostRepository;
	private final UserRepository userRepository;

	// 후기 작성
	public ReviewResponseDTO createReview(Long userId, ReviewRequestDTO requestDTO) {
		if (!requestDTO.isValidEventType()) {
			throw new RuntimeException("하나의 이벤트 ID만 입력해야 합니다.");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

		Review review = new Review();
		review.setUser(user);
		review.setContent(requestDTO.getContent());
		review.setRating(requestDTO.getRating());
		review.setCreatedAt(LocalDateTime.now());
		review.setUpdatedAt(LocalDateTime.now());

		String eventTitle = "알 수 없음";

		if (requestDTO.getExhibitId() != null) {
			ExhibitEntity exhibit = exhibitRepository.findById(requestDTO.getExhibitId())
				.orElseThrow(() -> new RuntimeException("해당 전시를 찾을 수 없습니다."));
			review.setExhibit(exhibit);
			eventTitle = exhibit.getTitle();
		} else if (requestDTO.getFestivalId() != null) {
			FestivalPost festival = festivalPostRepository.findById(requestDTO.getFestivalId())
				.orElseThrow(() -> new RuntimeException("해당 축제를 찾을 수 없습니다."));
			review.setFestival(festival);
			eventTitle = festival.getFestivalContent();
		} else if (requestDTO.getPerformanceId() != null) {
			Performance performance = performanceRepository.findById(requestDTO.getPerformanceId())
				.orElseThrow(() -> new RuntimeException("해당 공연을 찾을 수 없습니다."));
			review.setPerformance(performance);
			eventTitle = performance.getPerformanceTitle();
		} else if (requestDTO.getPopupStoreId() != null) {
			PopupStorePost popupStore = popupStorePostRepository.findById(requestDTO.getPopupStoreId())
				.orElseThrow(() -> new RuntimeException("해당 팝업스토어를 찾을 수 없습니다."));
			review.setPopupStore(popupStore);
			eventTitle = popupStore.getContent();
		}

		List<ReviewImage> images = requestDTO.getImageUrls().stream()
			.map(url -> ReviewImage.builder().review(review).imageUrl(url).build())
			.collect(Collectors.toList());

		review.setImages(images);
		reviewRepository.save(review);

		return ReviewResponseDTO.fromEntity(review, eventTitle);
	}

	// 특정 이벤트의 후기 목록 조회
	public List<ReviewResponseDTO> getReviewsByEvent(Long exhibitId, Long festivalId, Long performanceId, Long popupStoreId) {
		List<Review> reviews;

		if (exhibitId != null) {
			reviews = reviewRepository.findByExhibitSeq(exhibitId);
		} else if (festivalId != null) {
			reviews = reviewRepository.findByFestivalId(festivalId);
		} else if (performanceId != null) {
			reviews = reviewRepository.findByPerformanceId(performanceId);
		} else if (popupStoreId != null) {
			reviews = reviewRepository.findByPopupStoreId(popupStoreId);
		} else {
			throw new RuntimeException("이벤트 ID가 필요합니다.");
		}

		return reviews.stream()
			.map(review -> ReviewResponseDTO.fromEntity(review, getEventTitle(review)))
			.collect(Collectors.toList());
	}

	// 후기 수정
	public ReviewResponseDTO updateReview(Long reviewId, Long userId, ReviewRequestDTO requestDTO) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new RuntimeException("해당 후기를 찾을 수 없습니다."));

		if (!review.getUser().getId().equals(userId)) {
			throw new RuntimeException("자신의 후기만 수정할 수 있습니다.");
		}

		review.setContent(requestDTO.getContent());
		review.setRating(requestDTO.getRating());
		review.setUpdatedAt(LocalDateTime.now());

		review.getImages().clear();
		List<ReviewImage> images = requestDTO.getImageUrls().stream()
			.map(url -> ReviewImage.builder().review(review).imageUrl(url).build())
			.collect(Collectors.toList());
		review.setImages(images);

		return ReviewResponseDTO.fromEntity(reviewRepository.save(review), getEventTitle(review));
	}

	// 후기 삭제
	public void deleteReview(Long reviewId, Long userId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new RuntimeException("해당 후기를 찾을 수 없습니다."));

		if (!review.getUser().getId().equals(userId)) {
			throw new RuntimeException("자신의 후기만 삭제할 수 있습니다.");
		}

		reviewRepository.delete(review);
	}

	// 이벤트 제목 조회 (후기 응답을 위한 메서드)
	private String getEventTitle(Review review) {
		if (review.getExhibit() != null) {
			return review.getExhibit().getTitle();
		} else if (review.getFestival() != null) {
			return review.getFestival().getFestivalContent();
		} else if (review.getPerformance() != null) {
			return review.getPerformance().getPerformanceTitle();
		} else if (review.getPopupStore() != null) {
			return review.getPopupStore().getContent();
		}
		return "알 수 없음";
	}
}

