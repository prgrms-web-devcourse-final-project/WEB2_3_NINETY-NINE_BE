package com.example.onculture.domain.event.crawler;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InstagramCrawler implements CommandLineRunner {

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

    @Override
    public void run(String... args) {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            WebDriver driver = setupWebDriver();
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); //30초 대기
                //인스타그램 계정 ID, PW
                loginToInstagram(driver, wait, username, password);

                //Crawling 할 인스타 계정 주소
                driver.get("https://www.instagram.com/pops.official_/");
                // 스크롤 횟수를 20회로 지정 (더 많은 데이터 필요시 횟수 추가 가능)
                Set<String> postLinks = collectPostLinks(driver, 20);

                System.out.println("총 수집된 게시글 개수: " + postLinks.size());

                // 각 게시글에 접속하여 데이터 크롤링
                for (String postUrl : postLinks) {
                    driver.get(postUrl); // 게시글 페이지 접속
                    System.out.println("\n게시글 URL: " + postUrl);

                    // 게시글 내용 크롤링 (내용이 없으면 Null)
                    String postContent = fetchPostContent(wait);
                    // 게시글 이미지 URL 목록 크롤링
                    List<String> imageUrls = fetchImageUrls(wait);

                    // 크롤링한 데이터를 MariaDB에 저장 (내용 파싱 후 분리된 값 저장)
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

    // WebDriver 설정 133.xxx.xxx.xxx 으로 할때 오류가 발생
    private static WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().driverVersion("133").setup();
        return new ChromeDriver();
    }

    // Instagram 로그인
    private static void loginToInstagram(WebDriver driver, WebDriverWait wait, String username, String password) {
        driver.get("https://www.instagram.com/accounts/login/");
        try {
            // ID 입력
            WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            usernameInput.sendKeys(username);
            // 비밀번호 입력
            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.sendKeys(password);
            // 로그인 버튼 클릭
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
            loginButton.click();
            // 로그인 후 대기 (홈 화면 로딩 기다리기)
            wait.until(ExpectedConditions.urlContains("instagram.com"));
            System.out.println("로그인 성공!");
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
        }
    }

    // 프로필 페이지에서 게시글 링크 수집
    private static Set<String> collectPostLinks(WebDriver driver, int scrollCount) throws InterruptedException {
        Set<String> postLinks = new HashSet<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < scrollCount; i++) {
            // 현재 페이지 게시글 링크 저장
            List<WebElement> posts = driver.findElements(By.xpath("//a[contains(@href, '/p/')]"));
            for (WebElement post : posts) {
                String postUrl = post.getAttribute("href");
                postLinks.add(postUrl);
            }
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(3000); // 스크롤 후 데이터 로딩을 위한 3초 대기
        }
        return postLinks;
    }

    // 게시글 내용 크롤링 (없으면 Null)
    private static String fetchPostContent(WebDriverWait wait) {
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

    // 게시글 이미지 URL 목록 크롤링
    private static List<String> fetchImageUrls(WebDriverWait wait) {
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

    // 게시글 내용 파싱 후 DB 저장
    private static void savePost(Connection connection, String postUrl, String content, List<String> imageUrls) {
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
            System.out.println("데이터 베이스 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 이미지 URL들을 DB에 저장
    private static void savePostImages(Connection connection, long postId, List<String> imageUrls) throws SQLException {
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

    // 파싱된 게시글 내용 결과를 담는 헬퍼 클래스
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

    // 게시글 내용을 줄 단위로 파싱하여 필요한 정보를 분리
    private static ParsedContent parseContent(String content) {
        String location = null; // 장소
        String operatingDateStr = null; // 운영 날짜
        String operatingTime = null;    // 운영 시간
        StringBuilder detailsBuilder = new StringBuilder();

        String[] lines = content.split("\\r?\\n");
        // 이모지 제거후 DB에 저장
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

    // 운영 일자 데이터 처리
    private static java.sql.Date parseOperatingDate(String operatingDateStr) {
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
            dateStr = "2025/" + startDatePart; // 연도는 25년 고정
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
}
