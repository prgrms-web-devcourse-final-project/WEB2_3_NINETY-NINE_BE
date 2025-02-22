package com.example.onculture.global.utils.jwt;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class CustomUserDetailsService implements UserDetailsService {       // 인증 로직 ( UserService와 분리를 위해 커스텀 )

    private final UserRepository userRepository;

    // 사용자 이름(email)으로 사용자의 정보를 가져오는 메서드 ( login 메서드 내 인증에 사용됨 )
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new CustomUserDetails(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        // 중요 : DB에 저장되어 있는 Role을 호출 -> CustomUserDetails 객체의 Role 필드에 추가 -> CustomUserDetails에 있는 getAuthorities()를 통해 Spring Security의 GrantedAuthority로 변환됨
                        user.getRole()  // Role 필드 추가
                ))
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자가 존재하지 않습니다"));
    }
}
