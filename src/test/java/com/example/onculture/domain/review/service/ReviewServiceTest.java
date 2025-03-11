package com.example.onculture.domain.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.repository.ExhibitRepository;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ExhibitRepository exhibitRepository;

	@Mock
	private ReviewImageRepository reviewImageRepository;

	@Mock
	private S3Service s3Service;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private ReviewService reviewService;

	private User testUser;
	private Review testReview;
	private ReviewRequestDTO requestDTO;
	private MockMultipartFile testImage;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.id(1L)
			.email("test@example.com")
			.password("password")
			.nickname("TestUser")
			.build();

		testReview = new Review();
		testReview.setId(1L);
		testReview.setUser(testUser);
		testReview.setContent("테스트 리뷰");
		testReview.setRating(5);
		testReview.setCreatedAt(LocalDateTime.now());
		testReview.setUpdatedAt(LocalDateTime.now());

		requestDTO = new ReviewRequestDTO();
		requestDTO.setContent("새로운 리뷰");
		requestDTO.setRating(4);

		testImage = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test image".getBytes());
	}

	@Test
	@DisplayName("리뷰 생성 - 성공")
	void testCreateReview_Success() {
		// given
		requestDTO.setExhibitId(1L); // ✅ 정확히 하나의 이벤트 ID만 설정

		ExhibitEntity mockExhibit = new ExhibitEntity();
		mockExhibit.setSeq(1L);

		Review savedReview = new Review();
		savedReview.setId(1L);
		savedReview.setUser(testUser);
		savedReview.setContent(requestDTO.getContent());
		savedReview.setRating(requestDTO.getRating());
		savedReview.setExhibit(mockExhibit);
		savedReview.setImages(new ArrayList<>()); // 빈 이미지 리스트 초기화

		ReviewResponseDTO mockResponseDTO = new ReviewResponseDTO();
		mockResponseDTO.setId(1L);
		mockResponseDTO.setContent(requestDTO.getContent());
		mockResponseDTO.setRating(requestDTO.getRating());
		mockResponseDTO.setImageUrls(new ArrayList<>()); // 빈 리스트 초기화

		// ✅ 필요한 Repository 및 서비스 Mock 설정 추가
		when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		when(exhibitRepository.findBySeq(1L)).thenReturn(Optional.of(mockExhibit));
		when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
		when(s3Service.uploadFile(any(MockMultipartFile.class), eq("reviews"), anyString()))
			.thenReturn("https://s3.amazonaws.com/bucket/reviews/1.jpg");

		// ✅ ModelMapper Mock 추가
		when(modelMapper.map(any(Review.class), eq(ReviewResponseDTO.class))).thenReturn(mockResponseDTO);

		// when
		ReviewResponseDTO responseDTO = reviewService.createReview(1L, requestDTO, testImage);

		// then
		assertThat(responseDTO).isNotNull();
		assertThat(responseDTO.getContent()).isEqualTo(requestDTO.getContent());
		assertThat(responseDTO.getRating()).isEqualTo(requestDTO.getRating());
		assertThat(responseDTO.getImageUrls()).isNotNull(); // ✅ 이미지 URL 리스트가 null이 아닌지 확인

		verify(userRepository, times(1)).findById(1L);
		verify(exhibitRepository, times(1)).findBySeq(1L);
		verify(reviewRepository, times(1)).save(any(Review.class));
		verify(modelMapper, times(1)).map(any(Review.class), eq(ReviewResponseDTO.class)); // ✅ 추가된 검증
	}




	@Test
	@DisplayName("리뷰 생성 - 사용자 없음 예외")
	void testCreateReview_UserNotFound() {
		// given
		requestDTO.setExhibitId(1L); // ✅ 특정 이벤트 ID 설정 (INVALID_EVENT_TYPE 예외 방지)

		when(userRepository.findById(1L)).thenReturn(Optional.empty()); // ✅ 사용자 없음 설정

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(1L, requestDTO, testImage))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage()); // ✅ 예상 예외 메시지 수정

		verify(userRepository, times(1)).findById(1L); // ✅ 올바른 검증
	}


	@Test
	@DisplayName("리뷰 수정 - 성공")
	void testUpdateReview_Success() {
		// given
		testReview.setImages(new ArrayList<>()); // ✅ 기존 이미지 리스트 초기화

		ReviewResponseDTO mockResponseDTO = new ReviewResponseDTO();
		mockResponseDTO.setId(1L);
		mockResponseDTO.setContent(requestDTO.getContent());
		mockResponseDTO.setRating(requestDTO.getRating());
		mockResponseDTO.setImageUrls(new ArrayList<>()); // ✅ 이미지 리스트 초기화

		when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
		when(s3Service.uploadFile(any(MockMultipartFile.class), eq("reviews"), anyString()))
			.thenReturn("https://s3.amazonaws.com/bucket/reviews/1_updated.jpg");

		// ✅ ModelMapper Mock 추가
		when(modelMapper.map(any(Review.class), eq(ReviewResponseDTO.class))).thenReturn(mockResponseDTO);

		// when
		ReviewResponseDTO responseDTO = reviewService.updateReview(1L, 1L, requestDTO, testImage);

		// then
		assertThat(responseDTO).isNotNull();
		assertThat(responseDTO.getContent()).isEqualTo(requestDTO.getContent());
		assertThat(responseDTO.getImageUrls()).isNotNull(); // ✅ 이미지 URL 리스트가 null이 아닌지 확인

		verify(reviewRepository, times(1)).findById(1L);
		verify(reviewRepository, times(1)).save(testReview);
		verify(s3Service, times(1)).uploadFile(any(MockMultipartFile.class), eq("reviews"), anyString());
		verify(modelMapper, times(1)).map(any(Review.class), eq(ReviewResponseDTO.class)); // ✅ 추가된 검증
	}


	@Test
	@DisplayName("리뷰 수정 - 권한 없음 예외")
	void testUpdateReview_Unauthorized() {
		// given
		User anotherUser = User.builder().id(2L).build();
		testReview.setUser(anotherUser);

		when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

		// when & then
		assertThatThrownBy(() -> reviewService.updateReview(1L, 1L, requestDTO, testImage))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
	}

	@Test
	@DisplayName("리뷰 삭제 - 성공")
	void testDeleteReview_Success() {
		// given
		ReviewImage testReviewImage = ReviewImage.builder()
			.id(1L)
			.review(testReview)
			.imageUrl("https://s3.amazonaws.com/bucket/reviews/1.jpg")
			.build();
		testReview.setImages(new ArrayList<>(List.of(testReviewImage)));


		when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

		// when
		reviewService.deleteReview(1L, 1L);

		// then
		verify(reviewRepository, times(1)).delete(testReview);
		verify(s3Service, times(1)).deleteFile("reviews", "1.jpg");
	}

	@Test
	@DisplayName("리뷰 삭제 - 권한 없음 예외")
	void testDeleteReview_Unauthorized() {
		// given
		User anotherUser = User.builder().id(2L).build();
		testReview.setUser(anotherUser);

		when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

		// when & then
		assertThatThrownBy(() -> reviewService.deleteReview(1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
	}
}
