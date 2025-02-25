package com.example.onculture.domain.event.service;


import com.example.onculture.domain.event.model.PopupStorePost;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PopupStorePostService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${instagram.username}")
    private String username;

    @Value("${instagram.password}")
    private String password;

    // 기존 ParsedContent 헬퍼 클래스
    private static class ParsedContent {
        String location;
        java.sql.Date operatingDate;
        String operatingTime;
        String details;

        ParsedContent(String location, java.sql.Date operatingDate, String operatingTime, String details) {
            this.location = location;
            this.operatingDate = operatingDate;
            this.operatingTime = operatingTime;
            this.details = details;
        }
    }

    private final PopupStorePostRepository repository;

    public PopupStorePostService(PopupStorePostRepository repository) {
        this.repository = repository;
    }

    public PopupStorePost savePost(PopupStorePost post) {
        return repository.save(post);
    }

    public List<PopupStorePost> listAll() {
        return repository.findAll();
    }

    public List<PopupStorePost> searchByLocation(String keyword) {
        return repository.findByLocationContaining(keyword);
    }

    public void runCrawling() {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            WebDriver driver = setupWebDriver();
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                // 인스타그램 로그인
                loginToInstagram(driver, wait, username, password);
                // 크롤링할 인스타 계정 주소
                driver.get("https://www.instagram.com/pops.official_/");
                Set<String> postLinks = collectPostLinks(driver, 5);
                System.out.println("총 수집된 게시글 개수: " + postLinks.size());

                for (String postUrl : postLinks) {
                    driver.get(postUrl);
                    System.out.println("\n게시글 URL: " + postUrl);
                    String postContent = fetchPostContent(wait);
                    List<String> imageUrls = fetchImageUrls(wait);

                    // 파싱 (필요에 따라 수정)
                    ParsedContent pc = parseContent(postContent);

                    // 필수 데이터 체크 (예: 운영일자, 운영시간, 장소, 상세내용)
                    if (pc.operatingDate == null || pc.operatingTime == null || pc.location == null || pc.details == null || postContent.isEmpty()) {
                        System.out.println("필수 정보 누락되어 저장 건너뜀: " + postUrl);
                        continue;
                    }
                    savePost(dbConnection, postUrl, postContent, imageUrls);
                }
            } finally {
                driver.quit();
            }
        } catch (SQLException e) {
            System.out.println("데이터베이스 연결 실패: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Thread.sleep(3000);
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

    private ParsedContent parseContent(String content) {
        String location = null;
        String operatingDateStr = null;
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
                operatingDateStr = line.substring(1).trim();
            } else if (line.startsWith("⏰")) {
                operatingTime = line.substring(1).trim();
            } else {
                detailsBuilder.append(line).append("\n");
            }
        }
        String details = detailsBuilder.toString().trim();
        java.sql.Date operatingDate = parseOperatingDate(operatingDateStr);
        return new ParsedContent(location, operatingDate, operatingTime, details);
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
            dateStr = "2025/" + startDatePart; // 현재는 2025년 고정 (자동 연도 판별 로직으로 변경 가능)
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
                System.out.println("날짜 파싱 실패: " + e.getMessage());
            }
        }
        return null;
    }

    private void savePost(Connection connection, String postUrl, String content, List<String> imageUrls) {
        ParsedContent pc = parseContent(content);
        String insertPostSql = "INSERT INTO popup_store_post (post_url, content, operating_date, operating_time, location, details) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement postStmt = connection.prepareStatement(insertPostSql, Statement.RETURN_GENERATED_KEYS)) {
            postStmt.setString(1, postUrl);
            postStmt.setString(2, content);
            postStmt.setDate(3, pc.operatingDate);
            postStmt.setString(4, pc.operatingTime);
            postStmt.setString(5, pc.location);
            postStmt.setString(6, pc.details);

            if (postStmt.executeUpdate() == 0) {
                throw new SQLException("게시글 삽입 실패");
            }
            try (ResultSet generatedKeys = postStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long postId = generatedKeys.getLong(1);
                    savePostImages(connection, postId, imageUrls);
                } else {
                    throw new SQLException("게시글 삽입 실패");
                }
            }
        } catch (SQLException e) {
            System.out.println("데이터베이스 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePostImages(Connection connection, long postId, List<String> imageUrls) throws SQLException {
        Set<String> uniqueImageUrls = new HashSet<>(imageUrls);
        String insertImageSql = "INSERT INTO popup_store_post_images (post_id, image_url) VALUES (?, ?)";
        try (PreparedStatement imageStmt = connection.prepareStatement(insertImageSql)) {
            for (String imageUrl : uniqueImageUrls) {
                imageStmt.setLong(1, postId);
                imageStmt.setString(2, imageUrl);
                imageStmt.addBatch();
            }
            imageStmt.executeBatch();
        }
    }
}
