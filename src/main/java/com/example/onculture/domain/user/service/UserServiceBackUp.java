package com.example.onculture.domain.user.service;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserServiceBackUp implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // 사용자 이름(email)으로 사용자의 정보를 가져오는 메서드 ( login 메서드 내 인증에 사용됨 )
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자가 존재하지 않습니다"));
    }

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

//    public Long save(SignupRequestDTO dto) {
//
//        try {
//            log.info("회원가입 시도 - email: {}", dto.getEmail());
//
//            User user = userRepository.save(User.builder()
//                    .email(dto.getEmail())
//                    .password(passwordEncoder.encode(dto.getPassword()))
//                    .nickname(dto.getNickname())
//                    .build());
//
//            log.info("회원가입 성공 - userId: {}", user.getId());
//            return user.getId();
//
//        } catch (Exception e) {
//            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
//            throw new RuntimeException("회원가입 실패", e);
//        }
//    }


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
*/
