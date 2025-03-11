package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.PopupStorePost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import com.example.onculture.domain.event.util.RegionMapper;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.S3.S3Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
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

@Slf4j
@Service
public class PopupStoreService {

    @Value("${INSTAGRAM_ID}")
    private String username;

    @Value("${INSTAGRAM_PASSWORD}")
    private String password;

    private final PopupStorePostRepository popupStorePostRepository;

    private final BookmarkRepository bookmarkRepository;

    private final S3Service s3Service;

    public PopupStoreService(PopupStorePostRepository popupStorePostRepository, BookmarkRepository bookmarkRepository, S3Service s3Service) {
        this.popupStorePostRepository = popupStorePostRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.s3Service = s3Service;
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

    private String fetchAndUploadFirstImage(WebDriverWait wait) {
        try {
            List<WebElement> images = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//img[contains(@class, 'x5yr21d')]"))
            );

            if (images.isEmpty()) {
                log.warn("⚠️ 게시글에서 이미지 찾을 수 없음.");
                return null;
            }

            // 첫 번째 이미지 URL 가져오기
            String imageUrl = images.get(0).getAttribute("src");
            log.info("🔗 원본 이미지 URL: {}", imageUrl);

            // S3 업로드 후 URL 반환 (중복 방지)
            return uploadImageToS3(imageUrl);
        } catch (Exception e) {
            log.error("⚠️ 첫 번째 이미지 가져오기 실패: {}", e.getMessage());
            return null;
        }
    }

    // S3에 이미지 업로드 (중복 방지)
    private String uploadImageToS3(String imageUrl) {
        try {
            String fileName = generateFileName(imageUrl); // URL을 해시값으로 변환하여 파일명 통일

            // 기존에 같은 파일이 있는지 확인
            if (s3Service.doesFileExist("popup_store_posts", fileName)) {
                log.info("⚠️ 동일한 이미지가 이미 S3에 존재함: {}", s3Service.getFileUrl("popup_store_posts", fileName));
                return s3Service.getFileUrl("popup_store_posts", fileName);
            }

            // 존재하지 않으면 업로드
            String s3Url = s3Service.uploadFileFromUrl(imageUrl, "popup_store_posts", fileName);
            log.info("✅ S3 업로드 완료: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("❌ S3 업로드 실패: {}", e.getMessage());
            return null;
        }
    }

    // 이미지 URL을 MD5 해시로 변환하여 고유한 파일명 생성
    private String generateFileName(String imageUrl) {
        return DigestUtils.md5Hex(imageUrl) + ".jpg";
    }

    // S3 URL에서 파일명 추출
    private String extractFileNameFromUrl(String s3Url) {
        return s3Url.substring(s3Url.lastIndexOf("/") + 1);
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
                System.out.println("총 수집된 팝업스토어 게시글 개수: " + postLinks.size());

                for (String postUrl : postLinks) {
                    driver.get(postUrl);
                    System.out.println("\n📌 게시글 URL: " + postUrl);

                    String postContent = fetchPostContent(wait);
                    ParsedContent pc = parseContent(postContent);

                    // 필수 정보 검증 (위치 정보 없으면 저장 X)
                    if (pc.location == null || pc.location.trim().isEmpty()) {
                        System.out.println("❌ 필수 정보 누락으로 게시글 저장 건너뜀: " + postUrl);
                        continue;
                    }

                    // 이미지 URL 가져와서 S3 업로드
                    String s3ImageUrl = fetchAndUploadFirstImage(wait);

                    // PopupStorePost 엔티티 생성 및 설정
                    PopupStorePost post = new PopupStorePost();
                    post.setPostUrl(postUrl);
                    post.setContent(postContent);
                    post.setPopupsStartDate(pc.popupsStartDate);
                    post.setOperatingTime(pc.operatingTime);
                    post.setPopupsEndDate(pc.popupsEndDate);
                    post.setLocation(pc.location);
                    post.setDetails(pc.details);
                    post.setStatus(determineStatus(pc.popupsStartDate, pc.popupsEndDate));

                    // 지역 매핑 (RegionMapper 활용)
                    if (pc.location != null && !pc.location.trim().isEmpty()) {
                        String mappedRegion = RegionMapper.mapRegion(pc.location);
                        post.setPopupsArea(mappedRegion);
                    }

                    // S3에 업로드된 첫 번째 이미지 저장
                    if (s3ImageUrl != null) {
                        post.setImageUrls(Collections.singletonList(s3ImageUrl));
                    } else {
                        post.setImageUrls(Collections.emptyList()); // 이미지 없을 경우 빈 리스트 저장
                    }

                    try {
                        PopupStorePost savedPost = popupStorePostRepository.save(post);
                        System.out.println("✅ PopupStorePost 저장 완료! ID: " + savedPost.getId() + ", 상태: " + post.getStatus());
                    } catch (Exception e) {
                        System.out.println("❌ 게시글 저장 실패! S3에서 업로드된 이미지 삭제");

                        if (s3ImageUrl != null) {
                            s3Service.deleteFile("festival_posts", extractFileNameFromUrl(s3ImageUrl));
                            System.out.println("🗑 업로드된 이미지 삭제됨: " + s3ImageUrl);
                        }

                        throw new RuntimeException("게시글 저장 실패로 이미지 삭제 완료", e);
                    }
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
                            bookmarkRepository.findByUserIdAndPopupStorePostId(userId, popupStorePost.getId())
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
