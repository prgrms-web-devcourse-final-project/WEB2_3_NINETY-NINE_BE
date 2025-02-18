package com.example.onculture.domain.notification.controller;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.onculture.domain.notification.dto.NotificationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림 API", description = "사용자의 알림을 관리하는 API")
public class NotificationController {

	private final List<NotificationResponseDTO> mockNotifications = new ArrayList<>(Arrays.asList(
		new NotificationResponseDTO(1L, 2L, 1L, "COMMENT", "사용자1이 새로운 댓글을 달았습니다", 123L, "COMMENT", false, LocalDateTime.of(2025, 2, 15, 12, 0, 0)),
		new NotificationResponseDTO(2L, 2L, 3L, "LIKE", "사용자3이️ 게시글에 좋아요를 눌렀습니다", 456L, "POST", false, LocalDateTime.of(2025, 2, 15, 12, 30, 0)),
		new NotificationResponseDTO(3L, 2L, null, "DUEDATE", "북마크한 게시글이 곧 마감됩니다", 789L, "EVENT", false, LocalDateTime.of(2025, 2, 15, 14, 0, 0)),
		new NotificationResponseDTO(4L, 2L, null, "TICKET", "북마크한 게시글의 티켓팅 날짜가 다가왔습니다", 101L, "EVENT", false, LocalDateTime.of(2025, 2, 15, 16, 0, 0))
	));

	@Operation(summary = "특정 사용자의 모든 알림 조회", description = "userId에 해당하는 사용자의 모든 알림을 조회합니다.")
	@GetMapping("/{userId}")
	public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications(@PathVariable Long userId) {
		List<NotificationResponseDTO> userNotifications = new ArrayList<>();
		for (NotificationResponseDTO notification : mockNotifications) {
			if (notification.getUserId().equals(userId)) {
				userNotifications.add(notification);
			}
		}
		return ResponseEntity.ok(userNotifications);
	}

	@Operation(summary = "특정 사용자의 특정 알림 읽음 처리", description = "userId와 notiId에 해당하는 특정 알림을 읽음 처리합니다.")
	@PatchMapping("/{userId}/{notiId}/read")
	public ResponseEntity<String> markNotificationAsRead(@PathVariable Long userId, @PathVariable Long notiId) {
		for (NotificationResponseDTO notification : mockNotifications) {
			if (notification.getUserId().equals(userId) && notification.getNotiId().equals(notiId)) {
				notification.setRead(true);  // 실제 데이터 변경됨
				return ResponseEntity.ok("해당 사용자의 특정 알림이 읽음 처리되었습니다.");
			}
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 알림을 찾을 수 없습니다.");
	}

	@Operation(summary = "특정 사용자의 모든 알림 읽음 처리", description = "userId에 해당하는 모든 알림을 읽음 처리합니다.")
	@PatchMapping("/{userId}/read-all")
	public ResponseEntity<String> markAllNotificationsAsRead(@PathVariable Long userId) {
		boolean exists = false; // 해당 사용자의 알림이 존재하는지 확인
		for (NotificationResponseDTO notification : mockNotifications) {
			if (notification.getUserId().equals(userId)) {
				notification.setRead(true);  // 모든 알림 읽음 처리
				exists = true;
			}
		}

		if (exists) {
			return ResponseEntity.ok("해당 사용자의 모든 알림이 읽음 처리되었습니다.");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 알림을 찾을 수 없습니다.");
	}

	@Operation(summary = "특정 사용자의 특정 알림 삭제", description = "userId와 notiId에 해당하는 특정 알림을 삭제합니다.")
	@DeleteMapping("/{userId}/{notiId}")
	public ResponseEntity<String> deleteNotification(@PathVariable Long userId, @PathVariable Long notiId) {
		boolean removed = mockNotifications.removeIf(notification ->
			notification.getUserId().equals(userId) && notification.getNotiId().equals(notiId)
		);

		if (removed) {
			return ResponseEntity.ok("해당 사용자의 특정 알림이 삭제되었습니다.");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 알림을 찾을 수 없습니다.");
	}

	@Operation(summary = "특정 사용자의 모든 알림 삭제", description = "userId에 해당하는 모든 알림을 삭제합니다.")
	@DeleteMapping("/{userId}/delete-all")
	public ResponseEntity<String> deleteAllNotifications(@PathVariable Long userId) {
		boolean removed = mockNotifications.removeIf(notification -> notification.getUserId().equals(userId));

		if (removed) {
			return ResponseEntity.ok("해당 사용자의 모든 알림이 삭제되었습니다.");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 알림을 찾을 수 없습니다.");
	}

}
