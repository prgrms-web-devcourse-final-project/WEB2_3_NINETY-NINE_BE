package com.example.onculture.domain.review.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.user.domain.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exhibit_seq", referencedColumnName = "seq", nullable = true)
	private ExhibitEntity exhibit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "festival_id", nullable = true)
	private FestivalPost festival;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = true)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "popup_store_id", nullable = true)
	private PopupStorePost popupStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false)
	private int rating;

	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReviewImage> images = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();
}

