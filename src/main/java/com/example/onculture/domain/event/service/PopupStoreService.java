package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.event.util.RegionMapper;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.*;

@Service
public class PopupStoreService {

    @Value("${INSTAGRAM_ID}")
    private String username;

    @Value("${INSTAGRAM_PASSWORD}")
    private String password;

    private final PopupStorePostRepository popupStorePostRepository;

    private final BookmarkRepository bookmarkRepository;

    public PopupStoreService(PopupStorePostRepository popupStorePostRepository, BookmarkRepository bookmarkRepository) {
        this.popupStorePostRepository = popupStorePostRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    // WebDriver 설정: ChromeOptions를 사용하여 브라우저를 실제 사용자처럼 모방
    private WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().driverVersion("133").setup();
        ChromeOptions options = new ChromeOptions();
        // 실제 사용자 브라우저처럼 보이도록 headless 모드는 사용하지 않음
        options.addArguments("--start-maximized");
        // 자동화 탐지를 피하기 위한 옵션 추가 (필요시 더 보완)
        options.addArguments("--disable-blink-features=AutomationControlled");
        // 실제 브라우저의 User-Agent 사용
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        return new ChromeDriver(options);
    }

    // 인스타그램 로그인: 로그인 후 프로필 아이콘이 보일 때까지 대기하여 로그인 성공을 확실히 하고, 쿠키를 저장
    private void loginToInstagram(WebDriver driver, WebDriverWait wait, String username, String password) {
        driver.get("https://www.instagram.com/accounts/login/");

        // 로그인 페이지 완전 로딩 대기
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // 사용자명 입력 필드가 클릭 가능해질 때까지 대기
            WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            usernameInput.sendKeys(username);

            // 비밀번호 입력
            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.sendKeys(password);

            // 입력 후 약간의 대기 (사용자 입력 모방)
            Thread.sleep(2000);

            // 로그인 버튼 클릭
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();

            // 로그인 성공 후, 프로필 아이콘(또는 고유 요소)이 나타날 때까지 대기
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[contains(@alt, '프로필 사진')]")));
            System.out.println("로그인 성공!");

            // 로그인 상태 유지를 위해 쿠키 저장 (실제 환경에서는 파일 또는 안전한 스토리지에 저장할 것)
            saveCookies(driver);
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
        }
    }

    // 로그인 후 쿠키 정보를 저장 (예시로 콘솔 출력 – 보안 저장은 별도 구현)
    private void saveCookies(WebDriver driver) {
        Set<Cookie> cookies = driver.manage().getCookies();
        for (Cookie cookie : cookies) {
            System.out.println("쿠키 저장됨: " + cookie.getName() + " = " + cookie.getValue());
        }
        // 실제 운영환경에서는 이 쿠키들을 안전하게 파일이나 데이터베이스에 저장합니다.
    }

    // 쿠키를 로드하여 브라우저에 추가 (새 세션에서도 로그인 상태를 복원할 때 사용)
    private void loadCookies(WebDriver driver, Set<Cookie> cookies) {
        driver.get("https://www.instagram.com"); // 도메인 접근 필수
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        driver.navigate().refresh(); // 쿠키 적용을 위해 새로고침
    }


    private Set<String> collectPostLinks(WebDriver driver, int scrollCount) throws InterruptedException {
        Set<String> postLinks = new LinkedHashSet<>();
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

                    // 여러 이미지 중 첫 번째 이미지 URL만 선택하여 저장
                    String selectedImageUrl = null;
                    if (!imageUrls.isEmpty()) {
                        selectedImageUrl = imageUrls.get(0);
                    }
                    List<String> singleImageUrlList = new ArrayList<>();
                    if (selectedImageUrl != null) {
                        singleImageUrlList.add(selectedImageUrl);
                    }

                    ParsedContent pc = parseContent(postContent);

                    PopupStorePost post = new PopupStorePost();
                    post.setPostUrl(postUrl);
                    post.setContent(postContent);
                    post.setPopupsStartDate(pc.popupsStartDate);
                    post.setOperatingTime(pc.operatingTime);
                    post.setPopupsEndDate(pc.popupsEndDate);
                    post.setLocation(pc.location);
                    post.setDetails(pc.details);
                    post.setImageUrls(singleImageUrlList); // 단일 이미지 URL 리스트 설정
                    // 상태 결정 (현재 날짜와 운영/종료일 비교)
                    String status = determineStatus(pc.popupsStartDate, pc.popupsEndDate);
                    post.setStatus(status);

                    // 기존의 단순 토큰 추출 대신, RegionMapper를 사용하여 지역 매핑
                    if (pc.location != null && !pc.location.trim().isEmpty()) {
                        String mappedRegion = RegionMapper.mapRegion(pc.location);
                        post.setPopupsArea(mappedRegion);
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
