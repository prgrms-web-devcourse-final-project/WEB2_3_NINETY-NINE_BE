package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.*;
import com.example.onculture.domain.event.repository.*;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookmarkService {
    private final PerformanceRepository performanceRepository;
    private final ExhibitRepository exhibitRepository;
    private final FestivalPostRepository festivalPostRepository;
    private final PopupStorePostRepository popupStorePostRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    public String toggleBookmark(Long userId, Long eventPostId, String genre) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        switch (genre) {
            case "performance" -> togglePerformanceBookmark(user, eventPostId);
            case "exhibit" -> toggleExhibitBookmark(user, eventPostId);
            case "festival" -> toggleFestivalBookmark(user, eventPostId);
            case "popupStore" -> togglePopupStoreBookmark(user, eventPostId);
            default -> throw new CustomException(ErrorCode.INVALID_GENRE_REQUEST);
        }
        return "Toggled";
    }

    private void togglePerformanceBookmark(User user, Long eventPostId) {
        Performance performance = performanceRepository.findById(eventPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_CONTENT));
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndPerformanceId(user.getId(), eventPostId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .performance(performance)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    private void toggleExhibitBookmark(User user, Long eventPostId) {
        ExhibitEntity exhibit = exhibitRepository.findById(eventPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_CONTENT));
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndExhibitEntitySeq(user.getId(), eventPostId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .exhibitEntity(exhibit)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    private void toggleFestivalBookmark(User user, Long eventPostId) {
        FestivalPost festival = festivalPostRepository.findById(eventPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_CONTENT));
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndFestivalPostId(user.getId(), eventPostId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .festivalPost(festival)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    private void togglePopupStoreBookmark(User user, Long eventPostId) {
        PopupStorePost popupStore = popupStorePostRepository.findById(eventPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_CONTENT));
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndPopupStorePostId(user.getId(), eventPostId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .popupStorePost(popupStore)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }
}
