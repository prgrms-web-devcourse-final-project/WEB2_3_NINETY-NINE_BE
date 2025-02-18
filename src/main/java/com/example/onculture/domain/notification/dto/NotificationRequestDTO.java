package com.example.onculture.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequestDTO {
	private Long userId;       // 알림 받는 사용자 ID
	private Long senderId;     // 알림을 보낸 사용자 ID (nullable)
	private String type;       // 알림 유형 (예: COMMENT, LIKE, TICKET, DUEDATE)
	private String content;    // 알림 내용
	private Long relatedId;    // 게시글 또는 댓글 ID
	private String relatedType;// 'POST' or 'COMMENT' or 'EVENT' - 해당 게시글로 이동하기 위해 필요한 타입
}

