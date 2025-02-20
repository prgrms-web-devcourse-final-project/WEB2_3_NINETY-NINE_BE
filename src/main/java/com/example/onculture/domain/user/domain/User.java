package com.example.onculture.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor( access = AccessLevel.PROTECTED )
@Builder
@Table( name = "user" )
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)     // User Id가 변경되지 않도록 설정
    private Long id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)    // unique 제약조건 추가
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean socialFlag = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Social social;

    // createdAt: INSERT 시 자동 저장
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // deletedAt: NULL 가능, 필요할 때 값 설정
    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP NULL")
    private LocalDateTime deletedAt;

    @ColumnDefault("false")  // 'false'가 기본값
    private boolean deletedFlag = false; // Java에서도 기본값 설정

    @PrePersist
    public void prePersist() {
        this.social = this.social == null ? Social.Local: this.social;    // social 필드 기본값 설정
        this.role = this.role == null ? Role.USER : this.role;      // role 필드 기본값 설정
    }

    // 상속 받은 UserDetails 클래스를 사용하기 위한 필수 @Override 메서드
    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

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
        // 만료되었는지 확인하는 로직
        return true;    // true -> 만료되지 않았음
    }

    // 계정 잠금 여부 확인
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true;    // true -> 잠금되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true;    // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true;    // true -> 사용 가능
    }
}
