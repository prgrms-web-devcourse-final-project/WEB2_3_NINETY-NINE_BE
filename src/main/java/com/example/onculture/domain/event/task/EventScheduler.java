package com.example.onculture.domain.event.task;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.notification.domain.Notification;
import com.example.onculture.domain.notification.dto.NotificationRequestDTO;
import com.example.onculture.domain.notification.repository.NotificationRepository;
import com.example.onculture.domain.notification.service.NotificationService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class EventScheduler {

    private PerformanceRepository performanceRepository;
    private ExhibitRepository exhibitRepository;
    private FestivalPostRepository festivalPostRepository;
    private PopupStorePostRepository popupStorePostRepository;
    private BookmarkRepository bookmarkRepository;
    private NotificationService notificationService;

    // 공연(Performance) 처리: 상태 업데이트 및 종료된 데이터 삭제
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateAndCleanPerformances() {
        Date today = Date.valueOf(LocalDate.now());

        // 시작일 <= 오늘 <= 종료일 인데 상태가 "진행 예정"이면 "진행중"으로 변경
        List<Performance> performancesToUpdate = performanceRepository
                .findByPerformanceStateAndStartDateLessThanEqualAndEndDateGreaterThanEqual("진행 예정", today, today);
        for (Performance performance : performancesToUpdate) {
            performance.setPerformanceState("진행중");
        }
        performanceRepository.saveAll(performancesToUpdate);

        // 종료일 < 오늘인 공연은 삭제 (이미 종료된 데이터)
        List<Performance> performancesToDelete = performanceRepository.findByEndDateLessThan(today);
        performanceRepository.deleteAll(performancesToDelete);
    }

    // 전시회(ExhibitEntity) 처리: 상태 업데이트 및 종료된 데이터 삭제
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateAndCleanExhibits() {
        Date today = Date.valueOf(LocalDate.now());

        // 시작일 <= 오늘 <= 종료일 인데 상태가 "진행 예정"이면 "진행중"으로 변경
        List<ExhibitEntity> exhibitsToUpdate = exhibitRepository
                .findByExhibitStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual("진행 예정", today, today);
        for (ExhibitEntity exhibit : exhibitsToUpdate) {
            exhibit.setExhibitStatus("진행중");
        }
        exhibitRepository.saveAll(exhibitsToUpdate);

        // 종료일 < 오늘인 전시는 삭제
        List<ExhibitEntity> exhibitsToDelete = exhibitRepository.findByEndDateLessThan(today);
        exhibitRepository.deleteAll(exhibitsToDelete);
    }

    // 페스티벌(FestivalPost) 처리: 상태 업데이트 및 종료된 데이터 삭제
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateAndCleanFestivalPosts() {
        Date today = Date.valueOf(LocalDate.now());

        // festivalStartDate <= 오늘 <= festivalEndDate 인데 festivalStatus가 "진행 예정"이면 "진행중"으로 변경
        List<FestivalPost> postsToUpdate = festivalPostRepository
                .findByFestivalStatusAndFestivalStartDateLessThanEqualAndFestivalEndDateGreaterThanEqual("진행 예정", today, today);
        for (FestivalPost post : postsToUpdate) {
            post.setFestivalStatus("진행중");
        }
        festivalPostRepository.saveAll(postsToUpdate);

        // festivalEndDate < 오늘인 페스티벌 게시글은 삭제
        List<FestivalPost> postsToDelete = festivalPostRepository.findByFestivalEndDateLessThan(today);
        festivalPostRepository.deleteAll(postsToDelete);
    }

    // 팝업 스토어(PopupStorePost) 처리: 상태 업데이트 및 종료된 데이터 삭제
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateAndCleanPopupStorePosts() {
        Date today = Date.valueOf(LocalDate.now());

        // popupsStartDate <= 오늘 <= popupsEndDate 인데 status가 "진행 예정"이면 "진행중"으로 변경
        List<PopupStorePost> postsToUpdate = popupStorePostRepository
                .findByStatusAndPopupsStartDateLessThanEqualAndPopupsEndDateGreaterThanEqual("진행 예정", today, today);
        for (PopupStorePost post : postsToUpdate) {
            post.setStatus("진행중");
        }
        popupStorePostRepository.saveAll(postsToUpdate);

        // popupsEndDate < 오늘인 팝업 스토어 게시글은 삭제
        List<PopupStorePost> postsToDelete = popupStorePostRepository.findByPopupsEndDateLessThan(today);
        popupStorePostRepository.deleteAll(postsToDelete);
    }

    // 북마크한 게시글의 오픈 및 마감 알림 발송
    @Scheduled(cron = "0 2 0 * * ?") // 하루 1회 실행 (매일 00:02)
    public void sendBookmarkNotifications() {
        // 오늘 오픈하는 북마크 찾기
        List<Bookmark> openingBookmarks = bookmarkRepository.findBookmarksWithOpeningToday();
        for (Bookmark bookmark : openingBookmarks) {
            sendOpeningNotification(bookmark);
        }

        // 오늘 마감하는 북마크 찾기
        List<Bookmark> closingBookmarks = bookmarkRepository.findBookmarksWithClosingToday();
        for (Bookmark bookmark : closingBookmarks) {
            sendClosingNotification(bookmark);
        }
    }

    private void sendOpeningNotification(Bookmark bookmark) {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
            bookmark.getUser().getId(),
            null, // 보낸 사람 없음
            Notification.NotificationType.OPENING,
            "",
            bookmark.getExhibitEntity().getSeq(),
            Notification.RelatedType.EXHIBIT
        );

        notificationService.createNotification(requestDTO);
    }

    private void sendClosingNotification(Bookmark bookmark) {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
            bookmark.getUser().getId(),
            null,
            Notification.NotificationType.CLOSING,
            "",
            bookmark.getExhibitEntity().getSeq(),
            Notification.RelatedType.EXHIBIT
        );

        notificationService.createNotification(requestDTO);
    }

    @PostConstruct
    public void runAtStartup() {
        updateAndCleanPerformances();
        updateAndCleanExhibits();
        updateAndCleanFestivalPosts();
        updateAndCleanPopupStorePosts();
        sendBookmarkNotifications();
    }
}
