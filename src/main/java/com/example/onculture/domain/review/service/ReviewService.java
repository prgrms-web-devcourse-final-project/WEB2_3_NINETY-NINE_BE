package com.example.onculture.domain.review.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.review.domain.Review;
import com.example.onculture.domain.review.domain.ReviewImage;
import com.example.onculture.domain.review.dto.ReviewRequestDTO;
import com.example.onculture.domain.review.dto.ReviewResponseDTO;
import com.example.onculture.domain.review.repository.ReviewImageRepository;
import com.example.onculture.domain.review.repository.ReviewRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.S3.S3Service;

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
	private final ReviewImageRepository reviewImageRepository;
	private final S3Service s3Service;
	private final ModelMapper modelMapper;

	// 📌 후기 작성 (S3에 이미지 1장만 업로드)
	public ReviewResponseDTO createReview(Long userId, ReviewRequestDTO requestDTO, MultipartFile image) {
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

		// 📌 이벤트 연관 관계 설정
		setReviewEvent(review, requestDTO);
		reviewRepository.save(review);

		// 📌 이미지 업로드 (1장만 가능)
		if (image != null && !image.isEmpty()) {
			String imageUrl = s3Service.uploadFile(image, "reviews", review.getId() + ".jpg");
			ReviewImage reviewImage = ReviewImage.builder()
				.review(review)
				.imageUrl(imageUrl)
				.build();

			reviewImageRepository.save(reviewImage);
			review.setImages(List.of(reviewImage));
		}

		return mapToResponseDTO(review);
	}

	// 📌 특정 이벤트의 후기 목록 조회 (이미지 URL 포함)
	public List<ReviewResponseDTO> getReviewsByEvent(Long exhibitId, Long festivalId, Long performanceId, Long popupStoreId) {
		List<Review> reviews = fetchReviewsByEvent(exhibitId, festivalId, performanceId, popupStoreId);

		return reviews.stream()
			.map(this::mapToResponseDTO)
			.toList();
	}

	// 📌 후기 수정 (기존 이미지 삭제 후 새 이미지 업로드)
	public ReviewResponseDTO updateReview(Long reviewId, Long userId, ReviewRequestDTO requestDTO, MultipartFile image) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		review.setContent(requestDTO.getContent());
		review.setRating(requestDTO.getRating());
		review.setUpdatedAt(LocalDateTime.now());

		// 📌 기존 이미지 삭제 후 새로운 이미지 업로드
		if (image != null && !image.isEmpty()) {
			deleteExistingReviewImage(review);
			String imageUrl = s3Service.uploadFile(image, "reviews", review.getId() + ".jpg");
			ReviewImage reviewImage = ReviewImage.builder()
				.review(review)
				.imageUrl(imageUrl)
				.build();
			reviewImageRepository.save(reviewImage);
			review.setImages(List.of(reviewImage));
		}

		reviewRepository.save(review);
		return mapToResponseDTO(review);
	}

	// 📌 후기 삭제 (S3 이미지 삭제 포함)
	public void deleteReview(Long reviewId, Long userId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		deleteExistingReviewImage(review);
		reviewRepository.delete(review);
	}

	// 📌 기존 S3 이미지 삭제
	private void deleteExistingReviewImage(Review review) {

		if (review.getImages() == null || review.getImages().isEmpty()) {
			return;
		}
		ReviewImage image = review.getImages().get(0);
		String imageUrl = image.getImageUrl();
		String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		s3Service.deleteFile("reviews", fileName);
		review.getImages().clear();
		reviewImageRepository.delete(image);
	}

	// 📌 이벤트 타입 설정
	private void setReviewEvent(Review review, ReviewRequestDTO requestDTO) {
		if (requestDTO.getExhibitId() != null) {
			review.setExhibit(exhibitRepository.findBySeq(requestDTO.getExhibitId())
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
	}

	// 📌 특정 이벤트의 후기 목록 조회 (공연, 전시, 축제, 팝업스토어)
	private List<Review> fetchReviewsByEvent(Long exhibitId, Long festivalId, Long performanceId, Long popupStoreId) {
		if (exhibitId != null) {
			return reviewRepository.findByExhibitSeq(exhibitId);
		} else if (festivalId != null) {
			return reviewRepository.findByFestivalId(festivalId);
		} else if (performanceId != null) {
			return reviewRepository.findByPerformanceId(performanceId);
		} else if (popupStoreId != null) {
			return reviewRepository.findByPopupStoreId(popupStoreId);
		} else {
			throw new CustomException(ErrorCode.INVALID_EVENT_TYPE);
		}
	}

	// 📌 DTO 변환 (이미지 URL 포함)
	private ReviewResponseDTO mapToResponseDTO(Review review) {
		ReviewResponseDTO responseDTO = modelMapper.map(review, ReviewResponseDTO.class);
		responseDTO.setImageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList());
		return responseDTO;
	}
}





