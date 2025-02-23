package com.example.onculture.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.dto.NotificationResponseDTO;
import com.example.onculture.domain.notification.repository.NotificationRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)  // 빠른 단위 테스트를 위해 Spring Context를 로드하지 않음 나중에 통합 테스트 때 SpringBootTest로 변경
class NotificationServiceTest {

	// Mock 객체로 Repository, WebSocket, ModelMapper를 대체하여 DB 없이 테스트
	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private ModelMapper modelMapper;

	// @Mock으로 선언된 객체들을 자동 주입
	@InjectMocks
	private NotificationService notificationService;

	private Notification mockNotification;
	private NotificationRequestDTO requestDTO;
	private NotificationResponseDTO responseDTO;

	@BeforeEach // 테스트 실행 전에 공통적으로 필요한 데이터를 미리 설정하는 메서드
	void setUp() {
		// 자동 생성 되는 건 null 처리
		mockNotification = new Notification(
			null,  // notiId
			5L,  // userId (알림 받는 사용자)
			3L,  // senderId (알림을 보낸 사용자)
			Notification.NotificationType.COMMENT,  // 알림 유형
			"새 댓글이 달렸습니다.",  // 알림 내용
			100L,  // relatedId
			Notification.RelatedType.POST,  // 관련 타입
			false,  // 읽음 여부
			null  // createdAt
		);

		requestDTO = new NotificationRequestDTO(
			5L,  // userId (알림 받는 사용자)
			3L,  // senderId (알림을 보낸 사용자)
			Notification.NotificationType.COMMENT,  // 알림 유형
			"테스트 알림",  // 알림 내용
			100L,  // relatedId
			Notification.RelatedType.POST  // 관련 타입
		);

		responseDTO = new NotificationResponseDTO(
			null,  // notiId
			5L,  // userId (알림 받는 사용자)
			3L,  // senderId (알림을 보낸 사용자)
			Notification.NotificationType.COMMENT,  // 알림 유형
			"테스트 알림",  // 알림 내용
			100L,  // relatedId
			Notification.RelatedType.POST,  // 관련 타입
			false,  // 읽음 여부
			null  // createdAt
		);
	}


	// 알림 생성 테스트
	@Test // JUnit 테스트 메서드 지정
	@DisplayName("알림 생성 테스트") // 해당 설명이 테스트 이름으로 표시됨
	void testCreateNotification() {
		// GIVEN
		when(modelMapper.map(requestDTO, Notification.class)).thenReturn(mockNotification);
		when(notificationRepository.save(mockNotification)).thenReturn(mockNotification);
		when(modelMapper.map(mockNotification, NotificationResponseDTO.class)).thenReturn(responseDTO);

		// WHEN
		notificationService.createNotification(requestDTO);

		// THEN
		verify(notificationRepository, times(1)).save(mockNotification); // 저장 메서드 실행 확인 (1회 호출 된건지 확인)
		verify(messagingTemplate, times(1)).convertAndSend("/topic/notifications/5", responseDTO); // WebSocket 전송 확인
	}

	// 특정 사용자의 모든 알림 조회 테스트
	@Test
	@DisplayName("특정 사용자의 모든 알림 조회 테스트")
	void testGetAllNotifications() {
		// GIVEN
		List<Notification> mockNotifications = List.of(mockNotification);
		when(notificationRepository.findByUserId(5L)).thenReturn(mockNotifications);
		when(modelMapper.map(mockNotification, NotificationResponseDTO.class)).thenReturn(responseDTO);

		// WHEN
		List<NotificationResponseDTO> result = notificationService.getAllNotifications(5L);

		// THEN
		assertThat(result).isNotEmpty(); // 비어있는지 확인
		assertThat(result.get(0).getContent()).isEqualTo("테스트 알림"); // content 내용이 똑같은지 확인

		verify(notificationRepository, times(1)).findByUserId(5L); // DB 조회 확인 (1회 호출 된건지 확인)
	}

	// 특정 알림 읽음 처리 테스트
	@Test
	@DisplayName("특정 알림 읽음 처리 테스트")
	void testMarkNotificationAsRead() {
		// GIVEN
		when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(mockNotification)); // anyLong -> 어떤 값이 들어가더라도 작동, notiId null 처리해놨기 때문, null 처리 했기 때문에 Optional 사용

		// WHEN
		notificationService.markNotificationAsRead(5L, 1L);

		// THEN
		assertThat(mockNotification.getIsRead()).isTrue(); // 읽음 처리 됐는지 확인
		verify(notificationRepository, times(1)).save(mockNotification); // DB 저장 1회 호출 된건지 확인
	}


	// 특정 알림 읽음 처리 실패 테스트 (알림이 존재하지 않는 경우)
	@Test
	@DisplayName("특정 알림이 존재하지 않을 경우 예외 발생 테스트")
	void testMarkNotificationAsRead_NotFound() {
		// GIVEN
		when(notificationRepository.findById(1L)).thenReturn(Optional.empty());  // notiId가 1L로 조회할 때 데이터가 없다는걸 지정하기 위해 anyLong 대신 값 설정

		// WHEN & THEN
		assertThatThrownBy(() -> notificationService.markNotificationAsRead(5L, 1L)) // Optional.empty()가 반환됨으로 에러 반환 돼야함
			.isInstanceOf(CustomException.class) // 반환한 예외가 CustomException인지 확인
			.hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()); // 올바른 메시지가 포함되어있는지 확인
	}

	// 특정 사용자의 모든 알림 읽음 처리 테스트
	@Test
	@DisplayName("특정 사용자의 모든 알림 읽음 처리 테스트")
	void testMarkAllNotificationsAsRead() {
		// GIVEN
		List<Notification> mockNotifications = List.of(mockNotification);
		when(notificationRepository.findByUserId(5L)).thenReturn(mockNotifications);

		// WHEN
		notificationService.markAllNotificationsAsRead(5L);

		// THEN
		assertThat(mockNotification.getIsRead()).isTrue();
		verify(notificationRepository, times(1)).saveAll(mockNotifications);
	}

	// 특정 사용자의 모든 알림 읽음 처리 실패 테스트 (알림이 없을 경우)
	@Test
	@DisplayName("특정 사용자의 모든 알림이 존재하지 않을 경우 예외 발생 테스트")
	void testMarkAllNotificationsAsRead_NotFound() {
		// GIVEN
		when(notificationRepository.findByUserId(5L)).thenReturn(new ArrayList<>());

		// WHEN & THEN
		assertThatThrownBy(() -> notificationService.markAllNotificationsAsRead(5L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
	}

	// 특정 사용자의 모든 알림 삭제 테스트
	@Test
	@DisplayName("특정 사용자의 모든 알림 삭제 테스트")
	void testDeleteAllNotifications() {
		// GIVEN
		List<Notification> mockNotifications = List.of(mockNotification);
		when(notificationRepository.findByUserId(5L)).thenReturn(mockNotifications);

		// WHEN
		notificationService.deleteAllNotifications(5L);

		// THEN
		verify(notificationRepository, times(1)).deleteAll(mockNotifications);
	}

	// 특정 사용자의 모든 알림 삭제 실패 테스트 (알림이 없을 경우)
	@Test
	@DisplayName("특정 사용자의 모든 알림이 존재하지 않을 경우 예외 발생 테스트")
	void testDeleteAllNotifications_NotFound() {
		// GIVEN
		when(notificationRepository.findByUserId(5L)).thenReturn(new ArrayList<>());

		// WHEN & THEN
		assertThatThrownBy(() -> notificationService.deleteAllNotifications(5L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
	}
}
