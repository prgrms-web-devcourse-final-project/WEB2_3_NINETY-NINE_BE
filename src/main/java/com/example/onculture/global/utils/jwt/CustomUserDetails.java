package com.example.onculture.global.utils.jwt;

import com.example.onculture.domain.user.model.Role;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {     // 인증 객체 ( User Entity와 분리를 위해 커스텀 )

    private Long userId;
    private String email;
    private String password;
    private Role role;
//    private Set<Role> roles; // 다중 권한 지원을 위한 Set 사용

    // 상속 받은 UserDetails 클래스를 사용하기 위한 필수 @Override 메서드
    // getAuthorities() : 사용자 권한을 반환하는 역할
    // 단일 권한 부여 메서드 ( 한 사용자가 하나의 권한만 가짐 )
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security에서 권한 인식을 위해 "ROLE_" 접두사 추가
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // 다중권한 부여 메서드 ( 추후 필요할 때 사용 )
    /*
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());  // Set으로 변환하여 중복 방지
    }
     */

    // 사용자의 id를 반환(고유한 값)
    @Override
    public String getUsername() {
        return email;
    }

    // 사용자의 패스워드 반환
    @Override
    public String getPassword() {
        return password;
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 확인
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        return true;  // true -> 사용 가능
    }
}
