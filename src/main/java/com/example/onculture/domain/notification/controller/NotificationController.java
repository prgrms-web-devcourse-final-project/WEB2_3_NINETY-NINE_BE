package com.example.onculture.domain.notification.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.dto.NotificationResponseDTO;
import com.example.onculture.domain.notification.service.NotificationService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알림 API", description = "사용자의 알림을 관리하는 API")
public class NotificationController {

	private final NotificationService notificationService;

	@Operation(summary = "테스트용 알림 데이터 추가", description = "데이터베이스에 더미 알림 데이터를 저장합니다.")
	@PostMapping("/test")
	public ResponseEntity<SuccessResponse<NotificationResponseDTO>> createTestNotification(
		@RequestBody NotificationRequestDTO requestDTO) {
		NotificationResponseDTO responseDTO = notificationService.createTestNotification(requestDTO);
		return ResponseEntity.ok(SuccessResponse.success("테스트 알림이 생성되었습니다.", responseDTO));
	}

	// @Operation(summary = "특정 사용자의 모든 알림 조회", description = "userId에 해당하는 사용자의 모든 알림을 조회합니다.")
	// @GetMapping("/{userId}")
	// public ResponseEntity<SuccessResponse<List<NotificationResponseDTO>>> getUserNotifications(
	// 	@PathVariable Long userId) {
	// 	List<NotificationResponseDTO> notifications = notificationService.getAllNotifications(userId);
	// 	return ResponseEntity.ok(SuccessResponse.success("알림 목록 조회 성공", notifications));
	// }

	@Operation(summary = "현재 로그인한 사용자의 모든 알림 조회", description = "로그인한 사용자의 모든 알림을 조회합니다.")
	@GetMapping
	public ResponseEntity<SuccessResponse<List<NotificationResponseDTO>>> getUserNotifications(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		List<NotificationResponseDTO> notifications = notificationService.getAllNotifications(userDetails.getUserId());
		return ResponseEntity.ok(SuccessResponse.success("알림 목록 조회 성공", notifications));
	}

	// @Operation(summary = "특정 사용자의 특정 알림 읽음 처리", description = "userId와 notiId에 해당하는 특정 알림을 읽음 처리합니다.")
	// @PatchMapping("/{userId}/{notiId}/status")
	// public ResponseEntity<SuccessResponse<Void>> markAsRead(@PathVariable Long userId, @PathVariable Long notiId) {
	// 	notificationService.markNotificationAsRead(userId, notiId);
	// 	return ResponseEntity.ok(SuccessResponse.success("알림이 읽음 처리되었습니다.", null));
	// }

	@Operation(summary = "현재 로그인한 사용자의 특정 알림 읽음 처리", description = "로그인한 사용자의 특정 알림을 읽음 처리합니다.")
	@PatchMapping("/{notiId}/status")
	public ResponseEntity<SuccessResponse<Void>> markAsRead(
		@PathVariable @Parameter(description = "읽음 처리할 알림 ID") Long notiId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		notificationService.markNotificationAsRead(userDetails.getUserId(), notiId);
		return ResponseEntity.ok(SuccessResponse.success("알림이 읽음 처리되었습니다.", null));
	}

	// @Operation(summary = "특정 사용자의 모든 알림 읽음 처리", description = "userId에 해당하는 모든 알림을 읽음 처리합니다.")
	// @PatchMapping("/{userId}/all/status")
	// public ResponseEntity<SuccessResponse<Void>> markAllAsRead(@PathVariable Long userId) {
	// 	notificationService.markAllNotificationsAsRead(userId);
	// 	return ResponseEntity.ok(SuccessResponse.success("모든 알림이 읽음 처리되었습니다.", null));
	// }

	@Operation(summary = "현재 로그인한 사용자의 모든 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
	@PatchMapping("/all/status")
	public ResponseEntity<SuccessResponse<Void>> markAllAsRead(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		notificationService.markAllNotificationsAsRead(userDetails.getUserId());
		return ResponseEntity.ok(SuccessResponse.success("모든 알림이 읽음 처리되었습니다.", null));
	}

	// @Operation(summary = "특정 사용자의 모든 알림 삭제", description = "userId에 해당하는 모든 알림을 삭제합니다.")
	// @DeleteMapping("/{userId}")
	// public ResponseEntity<SuccessResponse<Void>> deleteAll(@PathVariable Long userId) {
	// 	notificationService.deleteAllNotifications(userId);
	// 	return ResponseEntity.ok(SuccessResponse.success("모든 알림이 삭제되었습니다.", null));
	// }

	@Operation(summary = "현재 로그인한 사용자의 모든 알림 삭제", description = "로그인한 사용자의 모든 알림을 삭제합니다.")
	@DeleteMapping
	public ResponseEntity<SuccessResponse<Void>> deleteAll(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		notificationService.deleteAllNotifications(userDetails.getUserId());
		return ResponseEntity.ok(SuccessResponse.success("모든 알림이 삭제되었습니다.", null));
	}

}
