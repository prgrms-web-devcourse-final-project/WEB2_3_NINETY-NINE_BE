package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.FestivalPost;
import com.example.onculture.domain.event.dto.EventPageResponseDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.FestivalPostRepository;
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
public class FestivalService {

	@Value("${INSTAGRAM_ID}")
	private String username;

	@Value("${INSTAGRAM_PASSWORD}")
	private String password;

	private final FestivalPostRepository festivalPostRepository;
	private final BookmarkRepository bookmarkRepository;
	private final S3Service s3Service;

	public FestivalService(FestivalPostRepository festivalPostRepository, BookmarkRepository bookmarkRepository,
		S3Service s3Service) {
		this.festivalPostRepository = festivalPostRepository;
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

	// 스크롤하여 게시글 URL들을 수집
	private Set<String> collectPostLinks(WebDriver driver, int scrollCount) throws InterruptedException {
		Set<String> postLinks = new LinkedHashSet<>();
		JavascriptExecutor js = (JavascriptExecutor)driver;
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

	// 게시글의 첫번째 이미지 URL을 S3에 업로드 후 반환 (중복 방지 포함)
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
			if (s3Service.doesFileExist("festival_posts", fileName)) {
				log.info("⚠️ 동일한 이미지가 이미 S3에 존재함: {}", s3Service.getFileUrl("festival_posts", fileName));
				return s3Service.getFileUrl("festival_posts", fileName);
			}

			// 존재하지 않으면 업로드
			String s3Url = s3Service.uploadFileFromUrl(imageUrl, "festival_posts", fileName);
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
		String cleaned = scheduleLine.replaceAll("\\s+", "")
			.replace("년", "/")
			.replace("월", "/")
			.replace("일", "");
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

	//랜덤 조회
	public List<EventResponseDTO> getRandomFestivalPosts(int randomSize, Long userId) {
		if (randomSize < 0) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return festivalPostRepository.findRandomFestivalPosts(randomSize)
			.stream()
			.map(festivalPost -> {
				boolean isBookmarked = userId != null &&
					bookmarkRepository.findByUserIdAndFestivalPostId(userId, festivalPost.getId())
						.isPresent();
				return new EventResponseDTO(festivalPost, isBookmarked);
			})
			.toList();
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

					// 게시글 필수 정보 확인 후 S3 업로드 실행
					ParsedFestivalEvent event = parseFestivalEvent(festivalPostContent);
					if (event.location.isEmpty()) {
						System.out.println("❌ 필수 정보 누락으로 게시글 저장 건너뜀: " + festivalPostUrl);
						continue;
					}

					// 게시글 객체 생성
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

					// 게시글이 저장된 후에 S3 업로드 실행
					String s3ImageUrl = fetchAndUploadFirstImage(wait);
					if (s3ImageUrl != null) {
						post.setImageUrls(Collections.singletonList(s3ImageUrl));
					} else {
						post.setImageUrls(Collections.emptyList());
					}

					try {
						// 최종적으로 게시글 저장
						FestivalPost savedPost = festivalPostRepository.save(post);
						System.out.println("✅ FestivalPost 저장 완료! ID: " + savedPost.getId() + ", 상태: " + status);
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

	// 공연/전시 상세정보 조회
	public EventResponseDTO getFestivalPostDetail(Long id, Long userId) {
		EventResponseDTO eventResponseDTO = festivalPostRepository.findById(id)
			.map(festivalPost -> {
				boolean isBookmarked = userId != null &&
					bookmarkRepository.findByUserIdAndPerformanceId(userId, festivalPost.getId())
						.isPresent();
				return new EventResponseDTO(festivalPost, isBookmarked);
			})
			.orElseThrow(() -> new RuntimeException("Performance not found with id: " + id));
		return eventResponseDTO;
	}

	public EventPageResponseDTO searchFestivalPosts(String region, String status, String titleKeyword, int pageNum,
		int pageSize, Long userId) {
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
			.map(festivalPost -> {
				boolean isBookmarked = userId != null &&
					bookmarkRepository.findByUserIdAndPerformanceId(userId, festivalPost.getId())
						.isPresent();
				return new EventResponseDTO(festivalPost, isBookmarked);
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