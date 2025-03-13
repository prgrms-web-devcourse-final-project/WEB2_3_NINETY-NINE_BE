package com.example.onculture.domain.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.dto.BookmarkEventListDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.PostWithLikeResponseDTO;
import com.example.onculture.domain.socialPost.dto.UserPostListResponseDTO;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.repository.ProfileRepository;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.utils.CookieUtil;
import com.example.onculture.global.utils.S3.S3Service;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.onculture.global.utils.CookieUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenService tokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private CustomUserDetails userDetails;
    @Mock
    private Authentication authentication;
    @Mock
    private S3Service s3Service;
    @Mock
    private AmazonS3 amazonS3;
    @Mock
    private ImageFileService imageFileService;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private SocialPostLikeRepository socialPostLikeRepository;
    @Mock
    private SocialPostRepository socialPostRepository;

    @Spy    // spy로 일부 메서드를 Mocking
    @InjectMocks
    private UserService userService;

    private User signupUser;
    private Profile signupProfile;
    private SignupRequestDTO signupRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private User findUser;
    private Profile findProfile;
    private ModifyRequestDTO modifyRequestDTO;
    private MultipartFile imageData;
    private User profileNullUser;

    // 테스트 전, 공통적으로 필요한 데이터 정의 및 생성
    @BeforeEach
    public void setUp() {
        // 회원가입 setUp
        signupRequestDTO = new SignupRequestDTO("fpdjxpa37@gmail.com", "!123456", "tester",
                Role.USER, LoginType.LOCAL_ONLY, new HashSet<>(Set.of(Social.LOCAL)));
        signupProfile = new Profile(1L, null, "", "", new HashSet<>(Set.of()));
        signupUser = new User(1L, "fpdjxpa37@gmail.com", "!123456", "tester", Role.USER,
                LoginType.LOCAL_ONLY, new HashSet<>(Set.of(Social.LOCAL)), LocalDateTime.now(), signupProfile);
        signupProfile.setUser(signupUser);      // Profile에 User 할당
        // 로그인 setUp
        loginRequestDTO = new LoginRequestDTO("fpdjxpa37@gmail.com", "!123456");
        // UserId 기반 사용자 프로필 정보 조회
        findProfile = new Profile(2L, null, "getUserProfile 테스트용 입니다.", "testimage.jpa",
                new HashSet<>(Set.of(Interest.C, Interest.D, Interest.E, Interest.F)));
        findUser = new User(2L, "fpdjxpa37@gmail.com", "!123456", "tester2", Role.USER,
                LoginType.LOCAL_ONLY, new HashSet<>(Set.of(Social.LOCAL)), LocalDateTime.now(), findProfile);
        findProfile.setUser(signupUser);
        // UserId 기반 사용자 프로필 정보 수정
        modifyRequestDTO = new ModifyRequestDTO("tester3", "encodedPassword", "modifyUserProfile 테스트용 입니다.",
                new HashSet<>(Set.of("팝업 스토어", "뮤지컬")), "testimage.jpa");
        imageData = new MockMultipartFile("profileImage", "testimage2.jpa", "image/jpeg", "fake image content".getBytes());
        // userId 기반 User 조회 및 Profile 존재 여부에 따른 처리
        profileNullUser = new User(3L, "fpdjxpa37@gmail.com", "!123456", "tester", Role.USER,
                LoginType.LOCAL_ONLY, new HashSet<>(Set.of(Social.LOCAL)), LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signup() {
        // Given
        String email = signupRequestDTO.getEmail();
        String nickname = signupRequestDTO.getNickname();

        // Mocking
        when(userRepository.existsByEmail(email)).thenReturn(false);   // 이메일이 빈값이라고 가정
        when(userRepository.existsByNickname(nickname)).thenReturn(false);     // 닉네임이 빈값이라고 가정
        when(modelMapper.map(signupRequestDTO, User.class)).thenReturn(signupUser);     // DTO를 User로 반환
        when(passwordEncoder.encode(signupRequestDTO.getPassword())).thenReturn("encodedPassword");     // 패스워드 인코딩

        // When
        userService.save(signupRequestDTO);

        // Then
        verify(userRepository, times(1)).save(signupUser);      // 저장 메서드 실행 확인
        assertEquals("encodedPassword", signupUser.getPassword());  // 비밀번호 인코딩 확인
    }

    @Test
    @DisplayName("로컬 로그인 테스트")
    void LocalLogin() {
        // Given
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();

        // Mocking Authentication 객체 생성
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // CustomUserDetails 생성 (CustomUserDetails 인증 정보를 담고 있는 객체)
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);
        when(userDetails.getEmail()).thenReturn(email);
        when(userDetails.getRole()).thenReturn(Role.USER);

        // Mocking jwtTokenProvider의 토큰 생성 메서드
        when(jwtTokenProvider.createAccessToken(1L, email, Role.USER)).thenReturn("mockAccessToken");
        when(tokenService.createRefreshToken(1L)).thenReturn("mockRefreshToken");

        // Mocking TokenService의 addAllTokenToCookie 메서드
        // void 메서드는 doNothing().when() 사용
        doNothing().when(tokenService).addAllTokenToCookie(request, response, "mockAccessToken", "mockRefreshToken");

        // When
        TokenResponse tokenResponse = userService.login(loginRequestDTO, request, response);

        // Then
        assertNotNull(tokenResponse);
        assertEquals("Bearer mockAccessToken", tokenResponse.getAccessToken());
        assertEquals("mockRefreshToken", tokenResponse.getRefreshToken());
    }

    @Test
    @DisplayName("로컬 로그아웃 테스트")
    void logout() {
        // Given
        String refreshToken = "mockRefreshToken";
        // Mocking
        doReturn(refreshToken).when(userService).extractRefreshToken(request);      // extractRefreshToken 내부 메서드라 @Spy 사용
        doNothing().when(tokenService).deleteRefreshToken(refreshToken);

        // When + Then (CookieUtil이 static이므로 try-with-resources 사용)
        try (MockedStatic<CookieUtil> mockedStaticUtil = Mockito.mockStatic(CookieUtil.class)) {        // CookieUtil는 static이기 때문에 MockedStatic로 호출됐는지 검증
            userService.logout(request, response);

            // 검증1: deleteRefreshToken 실행 검증
            verify(tokenService, times(1)).deleteRefreshToken(refreshToken);

            // 검증2: deleteCookie 실행 검증
            mockedStaticUtil.verify(() -> CookieUtil.deleteCookie(request, response, CookieUtil.REFRESH_TOKEN_COOKIE_NAME), times(1));
        }
    }

    @Test
    @DisplayName("재발급 메서드")
    void refreshToken() {
        // Given
        String refreshToken = "mockRefreshToken";
        String accessToken = "mockAccessToken";
        // Mocking
        doReturn(refreshToken).when(userService).extractRefreshToken(request);
        when(tokenService.createAccessTokenFromRefreshToken(refreshToken)).thenReturn(accessToken);

        // When + Then (CookieUtil이 static이므로 try-with-resources 사용)
        try (MockedStatic<CookieUtil> mockedStaticUtil = Mockito.mockStatic(CookieUtil.class)) {
            userService.refreshToken(request, response);

            // 검증1: createAccessTokenFromRefreshToken 실행 검증
            verify(tokenService, times(1)).createAccessTokenFromRefreshToken(refreshToken);

            // 검증2: addCookie 실행 검증
            mockedStaticUtil.verify(() -> CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_TOKEN_COOKIE_DURATION), times(1));
        }
    }

    @Test
    @DisplayName("닉네임 중복 메서드")
    void checkNickname() {
        // Given
        String nickname = "tester";
        when(userRepository.existsByNickname(nickname)).thenReturn(false); // existsByNickname()을 Mock으로 설정

        // When
        boolean result = userService.checkNickname(nickname);

        // Then
        assertNotNull(result);
        assertEquals(false, result);
        verify(userRepository, times(1)).existsByNickname(nickname);  // existsByNickname() 호출 검증
    }

    @Test
    @DisplayName("UserId 기반 사용자 프로필 정보 조회")
    void getUserProfile() {
        // Given
        Long userId = 2L;
        doReturn(findUser).when(userService).findUserAndProfileByuserId(userId);

        // ✅ 서비스 코드 변경에 맞춰 직접 URL을 profileImage 필드에 저장
        String profileImageUrl = "https://s3bucket/fpdjxpa37@gmail.com/profiles/testimage.jpa";
        findProfile.setProfileImage(profileImageUrl);

        // When
        UserProfileResponse response = userService.getUserProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(findUser.getNickname(), response.getNickname());
        assertEquals(findUser.getLoginType(), response.getLoginType());

        // ✅ 프로필 이미지 URL이 profile.getProfileImage() 값과 일치해야 함
        assertEquals(profileImageUrl, response.getProfileImage());
        assertEquals(profileImageUrl, response.getS3Bucket());  // ✅ 필드명이 `s3Bucket`이므로 동일하게 반환

        // 관심사 검증
        Set<String> interests = findProfile.getInterests().stream()
            .map(Interest::getKor)
            .collect(Collectors.toSet());
        assertEquals(interests, response.getInterests());

        // 호출 검증
        verify(userService, times(1)).findUserAndProfileByuserId(userId);
    }

    @Test
    @DisplayName("UserId 기반 사용자 프로필 정보 수정")
    void modifyUserProfile() {
        // Given
        Long userId = findUser.getId();
        String newImageFileName = "testimage.jpa"; // ✅ 기존 파일명 유지
        String folder = "profiles";

        doReturn(findUser).when(userService).findUserAndProfileByuserId(userId);
        when(userRepository.existsByNickname(modifyRequestDTO.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(modifyRequestDTO.getPassword().trim())).thenReturn("encodedPassword");
        when(imageFileService.checkFileExtensionAndRename(imageData, findUser.getEmail()))
            .thenReturn(newImageFileName);

        // ✅ `uploadFile()` 호출 시 실제 S3 URL을 반환하도록 변경
        String uploadedImageUrl = "https://s3bucket/fpdjxpa37@gmail.com/profiles/" + newImageFileName;
        doReturn(uploadedImageUrl).when(s3Service).uploadFile(imageData, folder, newImageFileName);

        // When
        userService.modifyUserProfile(userId, modifyRequestDTO, imageData);

        // Then
        assertEquals(modifyRequestDTO.getNickname(), findUser.getNickname());
        assertEquals("encodedPassword", findUser.getPassword());
        assertEquals(modifyRequestDTO.getDescription(), findProfile.getDescription());
        assertEquals(uploadedImageUrl, findProfile.getProfileImage());  // ✅ 프로필 이미지 URL 검증

        // Interest 변환 후 비교 (String -> Interest 변환 필요)
        Set<String> expectedInterests = modifyRequestDTO.getInterests();
        Set<String> actualInterests = findProfile.getInterests().stream()
            .map(Interest::getKor)
            .collect(Collectors.toSet());
        assertEquals(expectedInterests, actualInterests);

        // 검증
        verify(userRepository, times(1)).save(findUser);
        verify(s3Service, times(1)).uploadFile(imageData, folder, newImageFileName);
    }


    @Test
    @DisplayName("프로필 이미지 파일 S3 저장 및 파일명 반환 메서드")
    void profileImageSave() {
        // Given
        String newImageFileName = "testimage.jpa";
        String folder = "profiles";

        // ✅ `uploadFile()`에서 실제 S3 URL을 반환하도록 Mock 설정
        String uploadedImageUrl = "https://s3bucket/fpdjxpa37@gmail.com/profiles/" + newImageFileName;
        when(imageFileService.checkFileExtensionAndRename(imageData, findUser.getEmail())).thenReturn(newImageFileName);
        doReturn(uploadedImageUrl).when(s3Service).uploadFile(imageData, folder, newImageFileName);

        // When
        String imageFileName = userService.profileImageSave(modifyRequestDTO, imageData, findUser);

        // Then
        assertEquals(uploadedImageUrl, imageFileName);
        verify(imageFileService, times(1)).checkFileExtensionAndRename(imageData, findUser.getEmail());
        verify(s3Service, times(1)).uploadFile(imageData, folder, newImageFileName);
    }




    @Test
    @DisplayName("userId 기반 User 조회 및 Profile 존재 여부에 따른 처리")
    void findUserAndProfileByuserId() {
        // Given
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(profileNullUser));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);     // 실제 메서드 내에서 생성되는 Profile 객체를 가져옴
        when(profileRepository.save(profileCaptor.capture())).thenAnswer(i -> i.getArgument(0));        // 가져온 Profile 객체를 반환

        // When
        User user = userService.findUserAndProfileByuserId(userId);

        // Then
        assertNotNull(user);
        assertEquals(userId, user.getId());

        Profile savedProfile = profileCaptor.getValue();
        assertNotNull(savedProfile);
        assertEquals("", savedProfile.getDescription());
        assertEquals("", savedProfile.getProfileImage());
        assertTrue(savedProfile.getInterests().isEmpty());
        assertEquals(user, savedProfile.getUser());

        verify(userRepository, times(1)).findById(userId);
        verify(profileRepository, times(1)).save(profileCaptor.getValue());     // 반환된 Profile 객체를 사용
    }

    @Test
    @DisplayName("로그인 인증 후 인증 객체 반환 메서드")
    void authenticate() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), "!123456"));

        // When
        Authentication authentication = userService.authenticate(loginRequestDTO);

        // Then
        assertNotNull(authentication);
        assertEquals(loginRequestDTO.getEmail(), authentication.getPrincipal());
        assertEquals("!123456", authentication.getCredentials());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("request 에 있는 쿠키에서 refreshToken 값 가져오기")
    void extractRefreshToken() {
        // Given
        Cookie refreshTokenCookie = new Cookie("refreshToken", "valid_refresh_token");
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});

        // When
        String result = userService.extractRefreshToken(request);

        // Then
        assertNotNull(result);
        assertEquals("valid_refresh_token", result);
    }

    @Test
    @DisplayName("회원 삭제 메서드")
    void deleteUser() {
        // Given
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(findUser));
        doNothing().when(userRepository).delete(findUser);
        doNothing().when(userRepository).flush();

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).delete(findUser);
        verify(userRepository, times(1)).flush();
    }

    @DisplayName("getBookmarkedEvents - 사용자 북마크 이벤트 목록 조회 요청")
    @Test
    void getBookmarkedEvents_returnsCorrectEventPageResponse() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Performance performance = new Performance();
        performance.setId(200L);

        Bookmark bookmark = Bookmark.builder()
                .id(300L)
                .performance(performance)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Bookmark> bookmarkPage = new PageImpl<>(List.of(bookmark), pageable, 1);
        when(bookmarkRepository.findAllByUserId(userId, pageable)).thenReturn(bookmarkPage);

        // when
        BookmarkEventListDTO response = userService.getBookmarkedEvents(userId, pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getPageNum());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getNumberOfElements());

        List<EventResponseDTO> posts = response.getPosts();
        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertTrue(posts.get(0).getBookmarked());
    }

    @Test
    @DisplayName("getSocialPostsByUser - 정상 조회")
    void testGetSocialPostsByUser_valid() {
        // given
        Long userId = 1L;
        int pageNum = 0;
        int pageSize = 10;
        Long loginUserId = 2L;

        when(userRepository.existsById(userId)).thenReturn(true);

        Profile testProfile = Profile.builder()
                .profileImage("testImage.jpg")
                .build();

        User testUser = User.builder()
                .id(1L)
                .build();
        testUser.setProfile(testProfile);
        testProfile.setUser(testUser);

        SocialPost testSocialPost = SocialPost.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        List<SocialPost> socialPosts = List.of(testSocialPost);
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<SocialPost> socialPostsPage = new PageImpl<>(socialPosts, pageable, socialPosts.size());

        when(socialPostRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(socialPostsPage);

        when(socialPostLikeRepository.findSocialPostIdsByUserIdAndSocialPostIds(eq(loginUserId), anyList()))
                .thenReturn(List.of(1L));

        // when
        UserPostListResponseDTO response = userService.getSocialPostsByUser(userId, pageNum, pageSize, loginUserId);

        // then
        assertNotNull(response);
        assertEquals(1, response.getPosts().size());
        PostWithLikeResponseDTO postDto = response.getPosts().get(0);
        assertEquals(1L, postDto.getId());
        assertTrue(postDto.isLikeStatus(), "좋아요 상태가 true여야 합니다.");

        assertEquals(1, response.getTotalPages());
        assertEquals(pageNum, response.getPageNum());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getNumberOfElements());

        verify(userRepository, times(1)).existsById(userId);
        verify(socialPostRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
        verify(socialPostLikeRepository, times(1))
                .findSocialPostIdsByUserIdAndSocialPostIds(eq(loginUserId), anyList());
    }
}
