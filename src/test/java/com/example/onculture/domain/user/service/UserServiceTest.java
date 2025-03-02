package com.example.onculture.domain.user.service;

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
import com.example.onculture.global.utils.AwsS3Util;
import com.example.onculture.global.utils.CookieUtil;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private AwsS3Util awsS3Util;
    @Mock
    private ImageFileService imageFileService;
    @Mock
    private ProfileRepository profileRepository;

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
        modifyRequestDTO = new ModifyRequestDTO("tester2", "!123456", "modifyUserProfile 테스트용 입니다.",
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
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());   // 이메일이 빈값이라고 가정
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.empty());     // 닉네임이 빈값이라고 가정
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
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.empty());     // 빈값 반환

        // When
        Boolean result = userService.checkNickname(nickname);

        // Then
        assertNotNull(result);
        assertEquals(false, result);
        verify(userRepository, times(1)).findByNickname(nickname);
    }

    @Test
    @DisplayName("UserId 기반 사용자 프로필 정보 조회")
    void getUserProfile() {
        // Given
        Long userId = 2L;
        doReturn(findUser).when(userService).findUserAndProfileByuserId(userId);

        String mockS3Url = "https://s3bucket/fpdjxpa37@gmail.com/testimage.jpg";
        doReturn(mockS3Url).when(awsS3Util).readFile(findUser.getProfile().getProfileImage());

        // When
        UserProfileResponse response = userService.getUserProfile(userId);

        // Then
        // DTO 검증
        assertNotNull(response);
        assertEquals(findUser.getNickname(), response.getNickname());
        assertEquals(findUser.getLoginType(), response.getLoginType());
        assertEquals(findProfile.getProfileImage(), response.getProfileImage());
        assertEquals(findProfile.getProfileImage(), response.getProfileImage());
        assertEquals(mockS3Url, response.getS3Bucket());

        // 관심사 검증
        Set<String> interests = findProfile.getInterests().stream()
                .map(Interest::getKor)
                .collect(Collectors.toSet());
        assertEquals(interests, response.getInterests());

        // 호출 검증
        verify(userService, times(1)).findUserAndProfileByuserId(userId);
        verify(awsS3Util, times(1)).readFile(findUser.getProfile().getProfileImage());
    }

    @Test
    @DisplayName("UserId 기반 사용자 프로필 정보 수정")
    void modifyUserProfile() {
        // Given
        Long userId = 2L;
        doReturn(findUser).when(userService).findUserAndProfileByuserId(userId);
        when(passwordEncoder.encode(modifyRequestDTO.getPassword().trim())).thenReturn("encodedPassword");     // 패스워드 인코딩
        String newImageFileName = "newImageFileName";
        when(imageFileService.checkFileExtensionAndRename(imageData, findUser.getEmail())).thenReturn(newImageFileName);
        doNothing().when(awsS3Util).uploadFile(imageData, newImageFileName);

        // When
        userService.modifyUserProfile(userId, modifyRequestDTO, imageData);

        // Then
        assertEquals(findUser.getNickname(), modifyRequestDTO.getNickname());
        assertEquals("encodedPassword", findUser.getPassword());
        assertEquals(findProfile.getDescription(), modifyRequestDTO.getDescription());
        assertEquals("newImageFileName", findProfile.getProfileImage());
        Set<String> interests = findProfile.getInterests().stream()
                        .map(Interest::getKor)
                                .collect(Collectors.toSet());
        assertEquals(interests, modifyRequestDTO.getInterests());

        verify(userRepository, times(1)).save(findUser);
        verify(awsS3Util, times(1)).uploadFile(imageData, newImageFileName);
    }

    @Test
    @DisplayName("userId 기반 User 조회 및 Profile 존재 여부에 따른 처리")
    void findUserAndProfileByuserId() {
        // Given
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(profileNullUser));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);     // 실제 메서드 내에서 생성되는 Profile 객체를 가져옴
        when(profileRepository.save(profileCaptor.capture())).thenAnswer(i -> i.getArgument(0));        // 가져온 Profile 객체를 반환
//        doNothing().when(profileRepository).save(profileCaptor.capture());

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
}
