package com.example.onculture.domain.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.dto.NotificationResponseDTO;
import com.example.onculture.domain.notification.repository.NotificationRepository;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final SimpMessagingTemplate messagingTemplate; // WebSocket을 통해 특정 클라이언트 또는 특정 주제(Topic)로 메시지를 보낼 때 사용됨
	private final SocialPostRepository socialPostRepository;
	private final ExhibitRepository exhibitRepository;
	private final PerformanceRepository performanceRepository;
	private final FestivalPostRepository festivalPostRepository;
	private final PopupStorePostRepository popupStorePostRepository;

	// 알림 기능 예정
	// 1. 사용자가 작성한 글에 좋아요 달리면 알림 - done
	// 2. 사용자가 작성한 게시글에 댓글 달리면  알림 - done
	// 3. 북마크한 게시글 오픈 시작 알림 - 오픈날 - done
	// 4. 북마크한 게시글 마감 임박 알림 - 마감날 - done
	// 5. 북마크한 게시글의 티켓 오픈하루전 - 데이터가 없음

	// 테스트용 알림 생성 로직
	public NotificationResponseDTO createTestNotification(NotificationRequestDTO requestDTO) {
		// userId, senderId를 User 객체로 변환
		User user = userRepository.findById(requestDTO.getUserId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		User sender = (requestDTO.getSenderId() != null) ?
			userRepository.findById(requestDTO.getSenderId())
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
			: null;

		// DTO → Entity 변환 후 저장
		Notification notification = new Notification(
			null, user, sender, requestDTO.getType(), requestDTO.getContent(),
			requestDTO.getRelatedId(), requestDTO.getRelatedType(), false, LocalDateTime.now()
		);

		notification = notificationRepository.save(notification);
		return modelMapper.map(notification, NotificationResponseDTO.class);

	}


	// 알림 생성 및 WebSocket 전송
	public void createNotification(NotificationRequestDTO requestDTO) {

		User user = userRepository.findById(requestDTO.getUserId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		User sender = (requestDTO.getSenderId() != null) ?
			userRepository.findById(requestDTO.getSenderId())
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
			: null;

		String content = generateContent(sender, requestDTO.getType(), requestDTO.getRelatedId(), requestDTO.getRelatedType());

		// Notification 객체 생성
		Notification notification = new Notification(
			null, user, sender, requestDTO.getType(), content,
			requestDTO.getRelatedId(), requestDTO.getRelatedType(), false, LocalDateTime.now()
		);

		notification = notificationRepository.save(notification);

		// Entity → ResponseDTO 변환 후 WebSocket으로 전송
		NotificationResponseDTO responseDTO = modelMapper.map(notification, NotificationResponseDTO.class);
		// 특정 사용자의 WebSocket 구독 경로로 메시지 전송
		messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), responseDTO);
	}

	// 특정 사용자의 모든 알림 조회
	public List<NotificationResponseDTO> getAllNotifications(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		List<Notification> notifications = notificationRepository.findAllByUser(user);
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
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Notification notification = notificationRepository.findById(notiId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

		// 요청한 사용자가 알림의 소유자인지 확인
		if (!notification.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		notification.setIsRead(true);
		notificationRepository.save(notification);
	}

	// 특정 사용자의 모든 알림 읽음 처리
	public void markAllNotificationsAsRead(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		List<Notification> notifications = notificationRepository.findAllByUser(user);
		if (notifications.isEmpty()) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		notifications.forEach(notification -> notification.setIsRead(true));
		notificationRepository.saveAll(notifications);
	}

	// 특정 사용자의 모든 알림 삭제
	public void deleteAllNotifications(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		List<Notification> notifications = notificationRepository.findAllByUser(user);
		if (notifications.isEmpty()) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		notificationRepository.deleteAll(notifications);
	}

	// 알림 메시지 생성 메서드
	private String generateContent(User sender, Notification.NotificationType type, Long relatedId, Notification.RelatedType relatedType) {
		String title = fetchTitleByRelatedId(relatedId, relatedType);

		if (sender == null) {
			Map<Notification.NotificationType, String> systemMessages = Map.of(
				Notification.NotificationType.TICKET, String.format("'%s'의 티켓 오픈이 하루 전입니다.", title),
				Notification.NotificationType.OPENING, String.format("'%s'이(가) 오픈되었습니다.", title),
				Notification.NotificationType.CLOSING, String.format("'%s'이(가) 마지막날입니다.", title)
			);
			return systemMessages.getOrDefault(type, "새로운 알림이 있습니다.");
		}

		Map<Notification.NotificationType, String> userMessages = Map.of(
			Notification.NotificationType.LIKE, String.format("%s님이 '%s' 게시글에 좋아요를 눌렀습니다.", sender.getNickname(), title),
			Notification.NotificationType.COMMENT, String.format("%s님이 '%s' 게시글에 댓글을 달았습니다.", sender.getNickname(), title)
		);

		return userMessages.getOrDefault(type, "새로운 알림이 있습니다.");
	}


	private String fetchTitleByRelatedId(Long relatedId, Notification.RelatedType relatedType) {
		if (relatedId == null || relatedType == null) {
			return "알 수 없는 게시글";
		}

		switch (relatedType) {
			case POST:
				return socialPostRepository.findById(relatedId)
					.map(SocialPost::getTitle)
					.orElse("삭제된 게시글");
			case EXHIBIT:
				return exhibitRepository.findById(relatedId)
					.map(ExhibitEntity::getTitle)
					.orElse("삭제된 전시");
			case PERFORMANCE:
				return performanceRepository.findById(relatedId)
					.map(Performance::getPerformanceTitle)
					.orElse("삭제된 공연");
			case FESTIVAL:
				return festivalPostRepository.findById(relatedId)
					.map(FestivalPost::getFestivalContent) //
					.orElse("삭제된 축제");
			case POPUPSTORE:
				return popupStorePostRepository.findById(relatedId)
					.map(PopupStorePost::getContent) //
					.orElse("삭제된 팝업스토어");
			default:
				return "알 수 없는 게시글";
		}
	}




}

//	  // createNotification 사용 방법
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
