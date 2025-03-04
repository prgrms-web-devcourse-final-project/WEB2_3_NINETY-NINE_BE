package com.example.onculture.domain.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;

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
	private final ModelMapper modelMapper;

	// 후기 작성
	public ReviewResponseDTO createReview(Long userId, ReviewRequestDTO requestDTO) {
		if (!requestDTO.isValidEventType()) {
			throw new CustomException(ErrorCode.INVALID_EVENT_TYPE);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Review review = new Review();
		review.setUser(user);
		review.setContent(requestDTO.getContent());
		review.setRating(requestDTO.getRating());
		review.setCreatedAt(LocalDateTime.now());
		review.setUpdatedAt(LocalDateTime.now());

		if (requestDTO.getExhibitId() != null) {
			review.setExhibit(exhibitRepository.findById(requestDTO.getExhibitId())
				.orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND)));
		} else if (requestDTO.getFestivalId() != null) {
			review.setFestival(festivalPostRepository.findById(requestDTO.getFestivalId())
				.orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND)));
		} else if (requestDTO.getPerformanceId() != null) {
			review.setPerformance(performanceRepository.findById(requestDTO.getPerformanceId())
				.orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND)));
		} else if (requestDTO.getPopupStoreId() != null) {
			review.setPopupStore(popupStorePostRepository.findById(requestDTO.getPopupStoreId())
				.orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND)));
		}

		List<ReviewImage> images = new ArrayList<>();
		for (String url : requestDTO.getImageUrls()) {
			ReviewImage reviewImage = new ReviewImage();
			reviewImage.setReview(review);
			reviewImage.setImageUrl(url);
			images.add(reviewImage);
		}
		review.setImages(images);
		reviewRepository.save(review);

		ReviewResponseDTO responseDTO = modelMapper.map(review, ReviewResponseDTO.class);
		responseDTO.setImageUrls(getImageUrls(review));
		return responseDTO;
	}

	// 특정 이벤트의 후기 목록 조회
	public List<ReviewResponseDTO> getReviewsByEvent(Long exhibitId, Long festivalId, Long performanceId, Long popupStoreId) {
		List<Review> reviews = new ArrayList<>();

		if (exhibitId != null) {
			reviews = reviewRepository.findByExhibitSeq(exhibitId);
		} else if (festivalId != null) {
			reviews = reviewRepository.findByFestivalId(festivalId);
		} else if (performanceId != null) {
			reviews = reviewRepository.findByPerformanceId(performanceId);
		} else if (popupStoreId != null) {
			reviews = reviewRepository.findByPopupStoreId(popupStoreId);
		} else {
			throw new CustomException(ErrorCode.INVALID_EVENT_TYPE);
		}

		List<ReviewResponseDTO> responseList = new ArrayList<>();
		for (Review review : reviews) {
			ReviewResponseDTO responseDTO = modelMapper.map(review, ReviewResponseDTO.class);
			responseDTO.setImageUrls(getImageUrls(review));
			responseList.add(responseDTO);
		}

		return responseList;
	}

	// 후기 수정
	public ReviewResponseDTO updateReview(Long reviewId, Long userId, ReviewRequestDTO requestDTO) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		review.setContent(requestDTO.getContent());
		review.setRating(requestDTO.getRating());
		review.setUpdatedAt(LocalDateTime.now());

		review.getImages().clear();
		List<ReviewImage> images = new ArrayList<>();
		for (String url : requestDTO.getImageUrls()) {
			ReviewImage reviewImage = new ReviewImage();
			reviewImage.setReview(review);
			reviewImage.setImageUrl(url);
			images.add(reviewImage);
		}
		review.setImages(images);
		reviewRepository.save(review);

		ReviewResponseDTO responseDTO = modelMapper.map(review, ReviewResponseDTO.class);
		responseDTO.setImageUrls(getImageUrls(review));
		return responseDTO;
	}

	// 후기 삭제
	public void deleteReview(Long reviewId, Long userId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		reviewRepository.delete(review);
	}

	// 이미지 URL 리스트 변환 (ModelMapper가 자동 매핑하지 못하는 부분)
	private List<String> getImageUrls(Review review) {
		List<String> imageUrls = new ArrayList<>();
		for (ReviewImage image : review.getImages()) {
			imageUrls.add(image.getImageUrl());
		}
		return imageUrls;
	}
}



