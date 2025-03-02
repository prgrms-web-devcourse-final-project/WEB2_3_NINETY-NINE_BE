package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @InjectMocks
    private UserService userService;

    private User signupUser;
    private Profile signupProfile;
    private SignupRequestDTO signupRequestDTO;
    private LoginRequestDTO loginRequestDTO;


    // 테스트 전, 공통적으로 필요한 데이터 정의 및 생성
    @BeforeEach
    public void setUp() {
        signupRequestDTO = new SignupRequestDTO(
                "fpdjxpa37@gmail.com",
                "!123456",
                "tester",
                Role.USER,
                LoginType.LOCAL_ONLY,
                new HashSet<>(Set.of(Social.LOCAL))
        );

        signupProfile = new Profile(1L, null, "", "", new HashSet<>(Set.of()));

        signupUser = new User(
                1L,
                "fpdjxpa37@gmail.com",
                "!123456",
                "tester",
                Role.USER,
                LoginType.LOCAL_ONLY,
                new HashSet<>(Set.of(Social.LOCAL)),
                LocalDateTime.now(),
                signupProfile
        );
        signupProfile.setUser(signupUser);      // Profile에 User 할당

        loginRequestDTO = new LoginRequestDTO("fpdjxpa37@gmail.com", "!123456");
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
        Authentication authentication = mock(Authentication.class);     // 인증이 완료된 Authentication 객체 반환
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // CustomUserDetails 생성 (CustomUserDetails 인증 정보를 담고 있는 객체)
        CustomUserDetails userDetails = mock(CustomUserDetails.class);      // CustomUserDetails 객체 반환
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);
        when(userDetails.getEmail()).thenReturn(email);
        when(userDetails.getRole()).thenReturn(Role.USER);

        // Mocking jwtTokenProvider의 토큰 생성 메서드
        when(jwtTokenProvider.createAccessToken(1L, email, Role.USER)).thenReturn("mockAccessToken");
        when(tokenService.createRefreshToken(1L)).thenReturn("mockRefreshToken");

        // Mocking TokenService의 addAllTokenToCookie 메서드
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        // void 메서드는 doNothing().when() 사용
        doNothing().when(tokenService).addAllTokenToCookie(request, response, "mockAccessToken", "mockRefreshToken");

        // When
        TokenResponse tokenResponse = userService.login(loginRequestDTO, request, response);

        // Then
        assertNotNull(tokenResponse);
        assertEquals("Bearer mockAccessToken", tokenResponse.getAccessToken());
        assertEquals("mockRefreshToken", tokenResponse.getRefreshToken());
    }
}
