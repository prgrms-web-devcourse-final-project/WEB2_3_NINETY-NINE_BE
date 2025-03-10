package com.example.onculture.domain.notification.dto;

import java.time.LocalDateTime;

import com.example.onculture.domain.notification.domain.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
	private Long notiId;       // 알림 ID
	private Notification.NotificationType type;      // 알림 유형
	private String content;    // 알림 내용
	private Long relatedId;    // 관련된 게시글/댓글 ID
	private Notification.RelatedType relatedType;
	private boolean isRead;    // 읽음 여부
	private LocalDateTime createdAt; // 생성 시간
}
