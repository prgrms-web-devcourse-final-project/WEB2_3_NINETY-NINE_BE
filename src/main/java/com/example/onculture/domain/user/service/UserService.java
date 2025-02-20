package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    // 회원가입 메서드
    public Long save(SignupRequestDTO dto) {

        String email = dto.getEmail();
        String nickname = dto.getNickname();

        // 중복 이메일 검증 로직 추가
        // isPresent() : Optional 객체에서 제공하는 메서드로, 해당 Optional 객체가 값을 가지고 있는지 여부를 확인하는 메서드 ( 값이 있을 경우 True )
        if (userRepository.findByEmail(email).isPresent()) {
            // 커스텀한 중복 이메일 예외
            throw new CustomException.DuplicateEmailException();
        }

        // 중복 닉네임 검증 로직 추가
        if (userRepository.findByNickname(nickname).isPresent()) {
            // 커스텀한 중복 닉네임 예외
            throw new CustomException.DuplicateNicknameException();
        }

        // 회원가입 처리
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

    // 로그인 인증 메서드
    public Authentication authenticate(LoginRequestDTO dto) {

        // 인증 객체 생성 ( 아직 인증된 객체는 아님 )
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        // 사용자를 인증 ( 비밀번호 검증 포함 / 내부적으로 UserDetailsService의 loadUserByUsername()을 호출 )
        return authenticationManager.authenticate(authenticationToken);
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
