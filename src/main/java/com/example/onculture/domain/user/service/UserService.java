package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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
}
