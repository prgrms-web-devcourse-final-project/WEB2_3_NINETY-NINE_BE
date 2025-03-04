package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PopupStorePostService {

    @Value("${INSTAGRAM_ID}")
    private String username;

    @Value("${INSTAGRAM_PASSWORD}")
    private String password;

    private final PopupStorePostRepository popupStorePostRepository;

    private final BookmarkRepository bookmarkRepository;

    public PopupStorePostService(PopupStorePostRepository popupStorePostRepository, BookmarkRepository bookmarkRepository) {
        this.popupStorePostRepository = popupStorePostRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    public List<PopupStorePost> listAll() {
        return popupStorePostRepository.findAll();
    }

    public List<PopupStorePost> searchByTitle(String title) {
        return popupStorePostRepository.findByContentContaining(title);
    }

    private WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().driverVersion("133").setup();
        return new ChromeDriver();
    }

    private void loginToInstagram(WebDriver driver, WebDriverWait wait, String username, String password) {
        driver.get("https://www.instagram.com/accounts/login/");
        try {
            WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            usernameInput.sendKeys(username);
            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.sendKeys(password);
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("instagram.com"));
            System.out.println("로그인 성공!");
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
        }
    }


    private Set<String> collectPostLinks(WebDriver driver, int scrollCount) throws InterruptedException {
        Set<String> postLinks = new HashSet<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < scrollCount; i++) {
            List<WebElement> posts = driver.findElements(By.xpath("//a[contains(@href, '/p/')]"));
            for (WebElement post : posts) {
                postLinks.add(post.getAttribute("href"));
            }
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(5000);
        }
        return postLinks;
    }

    private String fetchPostContent(WebDriverWait wait) {
        try {
            WebElement postContent = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(@class, '_ap3a')]"))
            );
            String content = postContent.getText();
            System.out.println("게시글 내용: " + content);
            return content;
        } catch (Exception e) {
            System.out.println("게시글 내용 없음.");
            return "";
        }
    }

    private List<String> fetchImageUrls(WebDriverWait wait) {
        List<String> imageUrls = new ArrayList<>();
        try {
            List<WebElement> images = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//img[contains(@class, 'x5yr21d')]"))
            );
            System.out.println("게시글 이미지:");
            for (WebElement img : images) {
                String src = img.getAttribute("src");
                imageUrls.add(src);
                System.out.println(src);
            }
        } catch (Exception e) {
            System.out.println("게시글 이미지 없음.");
        }
        return imageUrls;
    }

    // ParsedContent 헬퍼 클래스 (종료일자 필드 추가)
    private static class ParsedContent {
        String location;
        java.sql.Date popupsStartDate;
        java.sql.Date popupsEndDate;
        String operatingTime;
        String details;

        ParsedContent(String location, java.sql.Date popupsStartDate, java.sql.Date popupsEndDate, String operatingTime, String details) {
            this.location = location;
            this.popupsStartDate = popupsStartDate;
            this.popupsEndDate = popupsEndDate;
            this.operatingTime = operatingTime;
            this.details = details;
        }
    }

    // 운영일자 문자열(예: "📆2025년 4월 19일-20일 (토~일)")를 파싱하여 정보를 추출
    private ParsedContent parseContent(String content) {
        String location = null;
        String popupsStartDateStr = null;
        String popupsEndDateStr = null;
        String operatingTime = null;
        StringBuilder detailsBuilder = new StringBuilder();

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("📍")) {
                int cp = line.codePointAt(0);
                int emojiCharCount = Character.charCount(cp);
                location = line.substring(emojiCharCount).trim();
            } else if (line.startsWith("📆")) {
                // "📆" 라벨 뒤의 문자열에서 "-"가 있으면 시작일과 종료일을 분리
                String dateLine = line.substring(1).trim();
                if (dateLine.contains("-")) {
                    String[] dateParts = dateLine.split("-");
                    popupsStartDateStr = dateParts[0].trim();
                    popupsEndDateStr = dateParts[1].trim();
                } else {
                    popupsStartDateStr = dateLine;
                }
            } else if (line.startsWith("⏰")) {
                operatingTime = line.substring(1).trim();
            } else {
                detailsBuilder.append(line).append("\n");
            }
        }
        String details = detailsBuilder.toString().trim();
        java.sql.Date operatingDate = parseOperatingDate(popupsStartDateStr);
        java.sql.Date popupsEndDate = parsepopupsEndDate(popupsEndDateStr);
        return new ParsedContent(location, operatingDate, popupsEndDate, operatingTime, details);
    }

    private java.sql.Date parseOperatingDate(String operatingDateStr) {
        if (operatingDateStr == null || operatingDateStr.isEmpty()) {
            return null;
        }
        String[] parts = operatingDateStr.split("-");
        if (parts.length == 0) {
            return null;
        }
        String startDatePart = parts[0].trim().replaceAll("\\(.*?\\)", "").replaceAll("[^0-9/]", "").trim();
        SimpleDateFormat sdf = null;
        String dateStr = null;
        if (startDatePart.contains("/")) {
            dateStr = "2025/" + startDatePart; // 연도 고정 처리 (필요시 동적 연도 처리)
            sdf = new SimpleDateFormat("yyyy/MM/dd");
        } else if (startDatePart.matches("\\d{8,}")) {
            dateStr = startDatePart.substring(0, 8);
            sdf = new SimpleDateFormat("yyyyMMdd");
        }
        if (sdf != null && dateStr != null) {
            try {
                java.util.Date parsedDate = sdf.parse(dateStr);
                return new java.sql.Date(parsedDate.getTime());
            } catch (Exception e) {
                System.out.println("시작일자 파싱 실패: " + e.getMessage());
            }
        }
        return null;
    }

    private java.sql.Date parsepopupsEndDate(String popupsEndDateStr) {
        if (popupsEndDateStr == null || popupsEndDateStr.isEmpty()) {
            return null;
        }
        String[] parts = popupsEndDateStr.split("-");
        if (parts.length == 0) {
            return null;
        }
        String endDatePart = parts[0].trim().replaceAll("\\(.*?\\)", "").replaceAll("[^0-9/]", "").trim();
        SimpleDateFormat sdf = null;
        String dateStr = null;
        if (endDatePart.contains("/")) {
            dateStr = "2025/" + endDatePart; // 연도 고정 처리
            sdf = new SimpleDateFormat("yyyy/MM/dd");
        } else if (endDatePart.matches("\\d{8,}")) {
            dateStr = endDatePart.substring(0, 8);
            sdf = new SimpleDateFormat("yyyyMMdd");
        }
        if (sdf != null && dateStr != null) {
            try {
                java.util.Date parsedDate = sdf.parse(dateStr);
                return new java.sql.Date(parsedDate.getTime());
            } catch (Exception e) {
                System.out.println("종료일자 파싱 실패: " + e.getMessage());
            }
        }
        return null;
    }

    // 공연 상태 결정 로직: 현재 날짜와 시작/종료일 비교
    private String determineStatus(java.sql.Date startDate, java.sql.Date endDate) {
        java.util.Date today = new java.util.Date();
        if (endDate != null && today.after(endDate)) {
            return "진행 종료";
        } else if (startDate != null && today.before(startDate)) {
            return "진행 예정";
        } else if (startDate != null && endDate != null &&
                (!today.before(startDate) && !today.after(endDate))) {
            return "진행중";
        }
        return "상태 미정";
    }

    // 전체 크롤링 실행 로직 (JPA 방식으로 저장)
    public void runCrawling() {
        try {
            WebDriver driver = setupWebDriver();
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                loginToInstagram(driver, wait, username, password);
                driver.get("https://www.instagram.com/pops.official_/");
                Set<String> postLinks = collectPostLinks(driver, 10);
                System.out.println("총 수집된 게시글 개수: " + postLinks.size());
                for (String postUrl : postLinks) {
                    driver.get(postUrl);
                    System.out.println("\n게시글 URL: " + postUrl);
                    String postContent = fetchPostContent(wait);
                    List<String> imageUrls = fetchImageUrls(wait);
                    ParsedContent pc = parseContent(postContent);
                    if (pc.popupsStartDate == null || pc.operatingTime == null ||
                            pc.location == null || pc.details == null || postContent.isEmpty()) {
                        System.out.println("필수 정보 누락되어 저장 건너뜀: " + postUrl);
                        continue;
                    }
                    PopupStorePost post = new PopupStorePost();
                    post.setPostUrl(postUrl);
                    post.setContent(postContent);
                    post.setPopupsStartDate(pc.popupsStartDate);
                    post.setOperatingTime(pc.operatingTime);
                    post.setPopupsEndDate(pc.popupsEndDate);
                    post.setLocation(pc.location);
                    post.setDetails(pc.details);
                    post.setImageUrls(imageUrls);
                    // 상태 결정 (현재 날짜와 운영/종료일 비교)
                    String status = determineStatus(pc.popupsStartDate, pc.popupsEndDate);
                    post.setStatus(status); // 엔티티에 status 필드가 있다고 가정

                    // 지역 추출 로직: location에서 앞의 두 단어를 추출하여 popupsArea에 저장
                    String location = pc.location;
                    if (location != null && !location.trim().isEmpty()) {
                        String[] tokens = location.split("\\s+");
                        if (tokens.length >= 2) {
                            String popupsArea = tokens[0] + " " + tokens[1];
                            post.setPopupsArea(popupsArea);
                        }
                    }

                    PopupStorePost savedPost = popupStorePostRepository.save(post);
                    System.out.println("PopupStorePost 저장 완료! ID: " + savedPost.getId() + ", 상태: " + status);
                }
            } finally {
                driver.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<EventResponseDTO> getRandomPopupStorePosts(int randomSize, Long userId) {
        if (randomSize < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return popupStorePostRepository.findRandomPopupStorePosts(randomSize)
                .stream()
                .map(popupStorePost -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndPopupStorePostId(userId, popupStorePost.getId())
                                    .isPresent();
                    return new EventResponseDTO(popupStorePost, isBookmarked);
                })
                .toList();
    }


    // 공연/전시 상세정보 조회
    public EventResponseDTO getPopupStorePostDetail(Long id, Long userId) {
        EventResponseDTO eventResponseDTO = popupStorePostRepository.findById(id)
                .map(popupStorePost -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndPopupStorePostId(userId, popupStorePost.getId())
                                    .isPresent();
                    return new EventResponseDTO(popupStorePost, isBookmarked);
                })
                .orElseThrow(() -> new RuntimeException("popupStorePost not found with id: " + id));
        return eventResponseDTO;
    }

    public EventPageResponseDTO searchPopupStorePosts(String region, String status, String titleKeyword, int pageNum, int pageSize, Long userId) {
        Specification<PopupStorePost> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 제목 키워드 검색 (대소문자 구분 없이)
            if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
                Expression<String> titleExpression = root.get("content").as(String.class);
                Expression<String> lowerTitle = criteriaBuilder.lower(titleExpression);

                predicates.add(
                        criteriaBuilder.like(
                                lowerTitle,
                                "%" + titleKeyword.toLowerCase() + "%"
                        )
                );
            }

            // 지역 필터
            if (region != null && !region.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("popupsArea"), region));
            }

            // 공연 상태(공연중, 공연예정) 필터
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<PopupStorePost> performancePage = popupStorePostRepository.findAll(spec, pageable);

        List<EventResponseDTO> posts = performancePage.getContent()
                .stream()
                .map(popupStorePost -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndPerformanceId(userId, popupStorePost.getId())
                                    .isPresent();
                    return new EventResponseDTO(popupStorePost, isBookmarked);
                })
                .toList();

        EventPageResponseDTO response = new EventPageResponseDTO();
        response.setPosts(posts);
        response.setTotalPages(performancePage.getTotalPages());
        response.setTotalElements(performancePage.getTotalElements());
        response.setPageNum(performancePage.getNumber());
        response.setPageSize(performancePage.getSize());
        response.setNumberOfElements(performancePage.getNumberOfElements());

        return response;
    }
}
