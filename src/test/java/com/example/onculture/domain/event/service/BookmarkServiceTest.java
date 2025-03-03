package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.dto.BookmarkEventListDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private ExhibitRepository exhibitRepository;

    @Mock
    private FestivalPostRepository festivalPostRepository;

    @Mock
    private PopupStorePostRepository popupStorePostRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    @DisplayName("toggleBookmark - 공연: 북마크가 없으면 추가 요청")
    @Test
    void toggleBookmark_whenPerformanceBookmarkNotExists_thenAddBookmark() {
        // given
        Long userId = 1L;
        Long performanceId = 100L;
        String genre = "performance";

        User user = User.builder().id(userId).build();

        Performance performance = new Performance();
        performance.setId(performanceId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId))
                .thenReturn(Optional.empty());

        // when
        String result = bookmarkService.toggleBookmark(userId, performanceId, genre);

        // then
        verify(bookmarkRepository).save(any(Bookmark.class));
        assertEquals("Toggled", result);
    }

    @DisplayName("toggleBookmark - 공연: 북마크가 이미 존재하면 삭제 요청")
    @Test
    void toggleBookmark_whenPerformanceBookmarkExists_thenDeleteBookmark() {
        // given
        Long userId = 1L;
        Long performanceId = 100L;
        String genre = "performance";

        User user = User.builder().id(userId).build();

        Performance performance = new Performance();
        performance.setId(performanceId);

        Bookmark bookmark = Bookmark.builder()
                .id(50L)
                .user(user)
                .performance(performance)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(bookmarkRepository.findByUserIdAndPerformanceId(userId, performanceId))
                .thenReturn(Optional.of(bookmark));

        // when
        String result = bookmarkService.toggleBookmark(userId, performanceId, genre);

        // then
        verify(bookmarkRepository).delete(bookmark);
        assertEquals("Toggled", result);
    }
}
