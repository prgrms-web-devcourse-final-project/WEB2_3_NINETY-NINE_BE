package com.example.onculture.domain.review.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {

	private Long exhibitId;
	private Long festivalId;
	private Long performanceId;
	private Long popupStoreId;

	@NotBlank(message = "내용은 필수입니다.")
	@Size(max = 1000, message = "내용은 최대 1000자까지 작성 가능합니다.")
	private String content;

	@Min(value = 1, message = "별점은 최소 1 이상이어야 합니다.")
	@Max(value = 5, message = "별점은 최대 5 이하이어야 합니다.")
	private int rating;

	private List<MultipartFile> images;

	public boolean isValidEventType() {
		int count = 0;
		if (exhibitId != null) count++;
		if (festivalId != null) count++;
		if (performanceId != null) count++;
		if (popupStoreId != null) count++;
		return count == 1; // 반드시 하나의 ID만 입력되어야 함
	}
}

