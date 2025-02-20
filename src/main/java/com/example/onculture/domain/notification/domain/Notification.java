package com.example.onculture.domain.notification.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Notification")
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notiId;

	@Column(nullable = false)
	private Long userId; // 알림 받는 사용자

	@Column(nullable = true)
	private Long senderId; // 알림을 보낸 사용자 (댓글 작성자, 좋아요 한 사용자 등)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type; // COMMENT, LIKE, TICKET, OPENING, CLOSING

	@Column(nullable = false, length = 255)
	private String content; // 알림 내용

	@Column(nullable = true)    // relatedId, relatedType 은 nullable -> 나중에 게시물과 관련 없는 알림을 보내야 할 수 도 있음으로
	private Long relatedId; // 자유게시글, 댓글, 이벤트 ID

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private RelatedType relatedType; // POST, COMMENT, EVENT

	@Column(nullable = false)
	private Boolean isRead = false; // 읽음 여부

	@CreationTimestamp    // 다른 팀원이 @CreatedDate 사용하면 통일 시키기
	private LocalDateTime createdAt; // 생성일시

	// Enum classes
	public enum NotificationType {
		COMMENT, LIKE, TICKET, OPENING, CLOSING
	}

	public enum RelatedType {
		POST, EVENT, COMMENT
	}
}



