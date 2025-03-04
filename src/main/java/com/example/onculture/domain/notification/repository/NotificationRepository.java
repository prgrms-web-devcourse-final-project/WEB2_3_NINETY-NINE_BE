package com.example.onculture.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.user.domain.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUser(User user);

	@EntityGraph(attributePaths = {"user", "sender"}) // user,sender 정보도 함께 필요할 때 사용
	List<Notification> findAllByUser(User user);

}
