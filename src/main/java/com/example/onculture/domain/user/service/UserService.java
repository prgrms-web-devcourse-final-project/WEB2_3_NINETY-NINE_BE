package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    // 회원가입 메서드
    @Transactional
    public User save(SignupRequestDTO dto) {

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

        // 이미 등록된 사용자 확인
        Optional<User> existingUser = userRepository.findByEmail(email);

        // 기존에 해당 이메일이 등록된 경우
        if (existingUser.isPresent()) {
            // 기존 사용자에 대해 socialUpdate 호출
            User user = existingUser.get();
            user.socialUpdate(Social.LOCAL, user.getSocials());  // 소셜 로그인 정보 업데이트
            return userRepository.save(user);
        }

        // 신규 사용자의 경우
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .role(Role.USER)
                .loginType(LoginType.LOCAL_ONLY)  // 기본적으로 LOCAL_ONLY로 설정
                .socials(new HashSet<>(Set.of(Social.LOCAL)))  // 기본 소셜 로그인 상태
                .build();

        // 회원가입 처리
        return userRepository.save(user);
    }

    // 로그인 인증 메서드
    public Authentication authenticate(LoginRequestDTO dto) {

        // 인증 객체 생성 ( 아직 인증된 객체는 아님 )
        // authenticate 내부 구조
        /*
        // principal (사용자 정보) = 사용자 이메일
        // credentials (비밀번호) = 입력한 비밀번호
        // authorities (권한 정보) = 권한 정보
        // authenticated (인증 여부) = false
         */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        // 사용자를 인증 ( 비밀번호 검증 포함 / 내부적으로 UserDetailsService의 loadUserByUsername()을 호출 )
        // authenticate 내부 구조
        /*
         principal (사용자 정보) = DB에서 가져온 사용자 객체
         credentials (비밀번호) = 보안상 제거됨 ( Null )
         authorities (권한 정보) = [ROLE_USER] ( 사용자의 권한 목록 )
         authenticated (인증 여부) = true
         */
        return authenticationManager.authenticate(authenticationToken);
    }

    // 유저ID로 유저 객체 조회 메서드
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));
    }
    // 이메일로 유저 객체 조회 메서드
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));
    }

    // 현재 사용자 인증 정보 조회 ( JWT 인증이 완료된 사용자 정보 조회 )
    public UserSimpleResponse userSimpleData(HttpServletRequest request, String accessToken) {

        // 현재 로그인된 사용자의 인증 정보를 가져올 때 사용
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("인증되지 않은 사용자입니다.");

        // 현재 로그인한 사용자 정보 가져오기
        UserSimpleResponse userDetails = (UserSimpleResponse) authentication.getPrincipal();

        return userDetails;
    }
}
