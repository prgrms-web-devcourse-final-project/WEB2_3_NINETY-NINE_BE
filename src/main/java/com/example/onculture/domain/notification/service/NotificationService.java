package com.example.onculture.domain.notification.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.dto.NotificationResponseDTO;
import com.example.onculture.domain.notification.repository.NotificationRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final ModelMapper modelMapper;
	private final SimpMessagingTemplate messagingTemplate; // WebSocket을 통해 특정 클라이언트 또는 특정 주제(Topic)로 메시지를 보낼 때 사용됨

	// 알림 기능 예정
	// 1. 사용자가 작성한 글에 좋아요 달리면 알림
	// 2. 사용자가 작성한 게시글에 댓글 달리면  알림
	// 3. 사용자가 작성한 댓글에 대댓글 달리면 알림
	// 4. 북마크한 게시글 오픈 시작 알림
	// 5. 북마크한 게시글 마감 임박 알림
	// 6. 북마크한 게시글의 티켓 오픈하루전

	// 테스트용 알림 생성 로직
	public NotificationResponseDTO createTestNotification(NotificationRequestDTO requestDTO) {
		Notification notification = modelMapper.map(requestDTO, Notification.class);
		notification.setIsRead(false); // 기본값 설정
		notification = notificationRepository.save(notification);
		return modelMapper.map(notification, NotificationResponseDTO.class);
	}


	// 알림 생성 및 WebSocket 전송
	public void createNotification(NotificationRequestDTO requestDTO) {

		if (requestDTO.getUserId() == null) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		// RequestDTO → Entity 변환 후 DB에 저장
		Notification notification = modelMapper.map(requestDTO, Notification.class);
		notification.setIsRead(false); // 기본값 설정
		notification = notificationRepository.save(notification);

		// Entity → ResponseDTO 변환 후 WebSocket으로 전송
		NotificationResponseDTO responseDTO = modelMapper.map(notification, NotificationResponseDTO.class);
		// 특정 사용자의 WebSocket 구독 경로로 메시지 전송
		messagingTemplate.convertAndSend("/topic/notifications/" + responseDTO.getUserId(), responseDTO);
	}

	// 특정 사용자의 모든 알림 조회
	public List<NotificationResponseDTO> getAllNotifications(Long userId) {
		List<Notification> notifications = notificationRepository.findByUserId(userId);
		if (notifications.isEmpty()) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		List<NotificationResponseDTO> responseDTOs = new ArrayList<>();
		for (Notification notification : notifications) {
			responseDTOs.add(modelMapper.map(notification, NotificationResponseDTO.class));
		}

		return responseDTOs; // List<NotificationResponseDTO> 형식
	}

	// 특정 알림 읽음 처리
	public void markNotificationAsRead(Long userId, Long notiId) {
		Notification notification = notificationRepository.findById(notiId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

		// 요청한 사용자가 알림의 소유자인지 확인
		if (!notification.getUserId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		notification.setIsRead(true);
		notificationRepository.save(notification);
	}

	// 특정 사용자의 모든 알림 읽음 처리
	public void markAllNotificationsAsRead(Long userId) {
		List<Notification> notifications = notificationRepository.findByUserId(userId);
		if (notifications.isEmpty()) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		notifications.forEach(notification -> notification.setIsRead(true));
		notificationRepository.saveAll(notifications);
	}

	// 특정 사용자의 모든 알림 삭제
	public void deleteAllNotifications(Long userId) {
		List<Notification> notifications = notificationRepository.findByUserId(userId);
		if (notifications.isEmpty()) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		notificationRepository.deleteAll(notifications);
	}

}

//		// createNotification 사용 방법
//    // 게시글 작성자에게 알림 전송
//     Long postOwnerId = getPostOwner(postId);
//     NotificationRequestDTO notificationDTO = new NotificationRequestDTO(
//         postOwnerId, senderId, Notification.NotificationType.COMMENT,
//         "회원님 게시글에 새로운 댓글이 달렸습니다.", postId, Notification.RelatedType.POST
//     );
//     notificationService.createNotification(notificationDTO);

// // 북마크한 게시글 오픈 시작 알림
// public void sendOpeningNotification(Long userId, Long postId) {
// 	NotificationRequestDTO notificationDTO = new NotificationRequestDTO(
// 		userId, null, Notification.NotificationType.OPENING,
// 		"북마크한 게시글이 오픈되었습니다.", postId, Notification.RelatedType.POST
// 	);
// 	notificationService.createNotification(notificationDTO);
// }
