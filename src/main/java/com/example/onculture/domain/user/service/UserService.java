package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    // 사용자 이름(email)으로 사용자의 정보를 가져오는 메서드
    @Override
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException((email)));
    }

    // 회원가입 메서드
    public Long save(SignupRequestDTO dto) {

        // 중복 이메일 검증 로직 추가
        // 중복 닉네임 검증 로직 추가

        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build()).getId();
    }

    // 회원가입 메서드 ( 에러 확인 메서드 )
    /*
    public Long save(SignupRequestDTO dto) {

        try {
            log.info("회원가입 시도 - email: {}", dto.getEmail());

            User user = userRepository.save(User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .build());

            log.info("회원가입 성공 - userId: {}", user.getId());
            return user.getId();

        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("회원가입 실패", e);
        }
    }
     */

    // 로그인 메서드 ( jwt 세팅 전까지는 랜덤 UUID 반환 )
    public String login(LoginRequestDTO dto) {

        // 이메일을 바탕으로 사용자 유무 확인
        User user = loadUserByUsername(dto.getEmail());

        // 이메일과 비밀번호를 사용하여 인증 시도
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getPassword());

        // authenticationManager로 인증 수행 ( 입력한 아이디와 비밀번호를 기반으로 새로운 인증 과정을 수행 )
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 인증 성공 시, 엑세스 토큰 생성 ( JWT 구현 전까지는 랜덤 코드 사용 )
        String accessToken = UUID.randomUUID().toString();

        return accessToken;
    }

    // 현재 사용자 인증 정보 조회 ( JWT 인증이 완료된 사용자 정보 조회 )
    public UserSimpleResponse userSimpleData(HttpServletRequest request, String accessToken) {

        // 현재 로그인된 사용자의 인증 정보를 가져올 때 사용
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        // 현재 로그인한 사용자 정보 가져오기
        UserSimpleResponse userDetails = (UserSimpleResponse) authentication.getPrincipal();

        return userDetails;
    }
}
