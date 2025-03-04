package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.dto.FestivalPostDTO;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.openqa.selenium.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FestivalPostService {

    @Value("${instagram.username}")
    private String username;

    @Value("${instagram.password}")
    private String password;

    private final FestivalPostRepository festivalPostRepository;

    public FestivalPostService(FestivalPostRepository festivalPostRepository) {
        this.festivalPostRepository = festivalPostRepository;
    }

    public List<FestivalPost> listAll() {
        return festivalPostRepository.findAll();
    }

    public List<FestivalPost> searchByTitle(String title) {
        return festivalPostRepository.findByFestivalContentContaining(title);
    }

    // 크롬 드라이버 설정 (버전 133으로 하드코딩)
    private WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().driverVersion("133").setup();
        return new ChromeDriver();
    }

    // 인스타그램 로그인 처리
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

    // 스크롤하여 게시글 URL들을 수집
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

    // 게시글의 텍스트 콘텐츠 추출 ('_ap3a' 클래스 사용)
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

    // 게시글의 이미지 URL 목록 추출 ('x5yr21d' 클래스 사용)
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

    // 파싱된 데이터를 담을 헬퍼 클래스
    private static class ParsedFestivalEvent {
        String title = "";
        String schedule = "";
        String location = "";
        String ticketPrice = "";
        String booking = "";
        String openTime = "";
        String details = "";
        java.sql.Date startDate = null;
        java.sql.Date endDate = null;
    }

    // 게시글 텍스트를 파싱하여 제목, 일정, 장소, 티켓 가격 등 필요한 정보를 추출
    private ParsedFestivalEvent parseFestivalEvent(String content) {
        ParsedFestivalEvent event = new ParsedFestivalEvent();
        String[] lines = content.split("\\r?\\n");
        String currentField = "";
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("일정:") || line.startsWith("공연 일정:")) {
                currentField = "";
                event.schedule = line.substring(line.indexOf(":") + 1).trim();
                event.startDate = parseStartDate(event.schedule);
                event.endDate = parseEndDate(event.schedule);
            } else if (line.startsWith("장소:") || line.startsWith("공연 장소:")) {
                currentField = "";
                event.location = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("가격:") || line.startsWith("티켓 가격:") ||
                    line.startsWith("가격") || line.startsWith("티켓 가격")) {
                currentField = "ticketPrice";
                if (line.contains(":")) {
                    event.ticketPrice = line.substring(line.indexOf(":") + 1).trim();
                } else {
                    event.ticketPrice = "";
                }
            } else if (line.startsWith("예매:") || line.startsWith("티켓 예매:")) {
                currentField = "";
                event.booking = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("오픈:") || line.startsWith("티켓 오픈:")) {
                currentField = "";
                event.openTime = line.substring(line.indexOf(":") + 1).trim();
            } else {
                if ("ticketPrice".equals(currentField)) {
                    if (line.startsWith("-")) {
                        event.ticketPrice += "\n" + line;
                    } else {
                        event.ticketPrice += "\n" + line;
                    }
                } else {
                    if (event.title.isEmpty()) {
                        event.title = line;
                    } else {
                        if (event.details.isEmpty()) {
                            event.details = line;
                        } else {
                            event.details += "\n" + line;
                        }
                    }
                }
            }
        }
        return event;
    }

    // 시작일자를 파싱 (예: "2025년 5월 30일" -> "2025/5/30")
    private java.sql.Date parseStartDate(String scheduleLine) {
        if (scheduleLine == null || scheduleLine.isEmpty()) {
            return null;
        }
        String cleaned = scheduleLine.replaceAll("\\s+", "").replace("년", "/")
                .replace("월", "/").replace("일", "");
        String[] parts = cleaned.split("~");
        String startDateStr = parts[0];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        try {
            java.util.Date parsed = sdf.parse(startDateStr);
            return new java.sql.Date(parsed.getTime());
        } catch (Exception e) {
            System.out.println("시작일자 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    // 종료일자를 파싱 (예: "2025년 5월 30일 ~ 20일" -> "2025/5/20")
    private java.sql.Date parseEndDate(String scheduleLine) {
        if (scheduleLine == null || scheduleLine.isEmpty()) {
            return null;
        }
        String cleaned = scheduleLine.replaceAll("\\s+", "")
                .replace("년", "/")
                .replace("월", "/")
                .replace("일", "");
        if (cleaned.contains("(")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("("));
        }
        String[] parts = cleaned.split("~");
        if (parts.length < 2) {
            return null;
        }
        String endPart = parts[1].trim();
        String startPart = parts[0].trim();
        String[] startComponents = startPart.split("/");
        if (startComponents.length < 3) {
            return null;
        }
        String year = startComponents[0];
        String month = startComponents[1];
        String endDateStr;
        if (endPart.contains("/")) {
            String[] endComponents = endPart.split("/");
            if (endComponents.length == 2) {
                endDateStr = year + "/" + endComponents[0] + "/" + endComponents[1];
            } else if (endComponents.length == 3) {
                endDateStr = endPart;
            } else {
                return null;
            }
        } else {
            endDateStr = year + "/" + month + "/" + endPart;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        try {
            java.util.Date parsed = sdf.parse(endDateStr);
            return new java.sql.Date(parsed.getTime());
        } catch (Exception e) {
            System.out.println("종료일자 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    // 공연 상태 결정 로직: 현재 날짜와 시작/종료일 비교
    private String determineStatus(java.sql.Date startDate, java.sql.Date endDate) {
        java.util.Date today = new java.util.Date();
        if (endDate != null && today.after(endDate)) {
            return "진행 종료";
        } else if (startDate != null && today.before(startDate)) {
            return "진행 예정";
        } else if (startDate != null && endDate != null && (!today.before(startDate) && !today.after(endDate))) {
            return "진행중";
        }
        return "상태 미정";
    }

    public List<FestivalPostDTO> getRandomFestivalPosts(int randomSize) {
        if (randomSize < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // festivalPostRepository.findRandomFestivalPosts(randomSize)는 무작위로 festival 데이터를 조회하는 커스텀 메서드입니다.
        return festivalPostRepository.findRandomFestivalPosts(randomSize)
                .stream()
                .map(FestivalPostDTO::new)  // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    // 전체 크롤링 실행 로직 (JPA 방식으로 엔티티 저장)
    public void runCrawling() {
        try {
            WebDriver driver = setupWebDriver();
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                loginToInstagram(driver, wait, username, password);
                driver.get("https://www.instagram.com/fstvl.life/");
                Set<String> postLinks = collectPostLinks(driver, 10);
                System.out.println("총 수집된 게시글 개수: " + postLinks.size());
                for (String festivalPostUrl : postLinks) {
                    driver.get(festivalPostUrl);
                    System.out.println("\n게시글 URL: " + festivalPostUrl);
                    String festivalPostContent = fetchPostContent(wait);
                    List<String> festivalImageUrls = fetchImageUrls(wait);
                    ParsedFestivalEvent event = parseFestivalEvent(festivalPostContent);
                    if (event.title.isEmpty() || event.schedule.isEmpty() ||
                            event.startDate == null || event.location.isEmpty()) {
                        System.out.println("필수 정보 누락되어 저장 건너뜀: " + festivalPostUrl);
                        continue;
                    }
                    FestivalPost post = new FestivalPost();
                    post.setFestivalPostUrl(festivalPostUrl);
                    String combinedContent = event.title + "\n" + event.schedule;
                    post.setFestivalContent(combinedContent);
                    post.setFestivalStartDate(event.startDate);
                    post.setFestivalEndDate(event.endDate);
                    post.setFestivalLocation(event.location);
                    post.setFestivalDetails(event.details);
                    post.setFestivalTicketPrice(event.ticketPrice);
                    String status = determineStatus(event.startDate, event.endDate);
                    post.setFestivalStatus(status);
                    // 이미지 URL 목록은 @ElementCollection으로 매핑된 필드에 설정
                    post.setImageUrls(festivalImageUrls);
                    FestivalPost savedPost = festivalPostRepository.save(post);
                    System.out.println("FestivalPost 저장 완료! ID: " + savedPost.getId() + ", 상태: " + status);
                }
            } finally {
                driver.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 공연/전시 상세정보 조회
    public EventResponseDTO getFestivalPostDetail(Long id) {
        EventResponseDTO eventResponseDTO = festivalPostRepository.findById(id)
                .map(EventResponseDTO::new)

                .orElseThrow(() -> new RuntimeException("Performance not found with id: " + id));
        return eventResponseDTO;
    }

    public EventPageResponseDTO searchFestivalPosts(String region, String status, String titleKeyword, int pageNum, int pageSize) {
        Specification<FestivalPost> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 제목 키워드 검색 (대소문자 구분 없이)
            if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
                Expression<String> titleExpression = root.get("festivalContent").as(String.class);
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
                predicates.add(criteriaBuilder.equal(root.get("festivalArea"), region));
            }

            // 공연 상태(공연중, 공연예정) 필터
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("festivalStatus"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<FestivalPost> performancePage = festivalPostRepository.findAll(spec, pageable);

        List<EventResponseDTO> posts = performancePage.getContent()
                .stream()
                .map(EventResponseDTO::new)
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