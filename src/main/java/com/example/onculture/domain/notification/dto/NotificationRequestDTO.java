package com.example.onculture.domain.notification.dto;

import com.example.onculture.domain.notification.domain.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
	private Long userId;       // 알림 받는 사용자 ID
	private Long senderId;     // 알림을 보낸 사용자 ID (nullable)
	private Notification.NotificationType type;       // 알림 유형 (예: COMMENT, LIKE, TICKET, OPENING, CLOSING)
	private String content;    // 알림 내용
	private Long relatedId;    // 게시글 또는 댓글 ID
	private Notification.RelatedType relatedType; // 'POST' or 'COMMENT' or 'EVENT' - 해당 게시글로 이동하기 위해 필요한 타입
}

