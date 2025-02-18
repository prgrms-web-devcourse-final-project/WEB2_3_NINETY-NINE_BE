package com.example.onculture.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table( name = "user" )
public class User {

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
    @ColumnDefault("'USER'")
    private Role role = Role.USER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Local'")
    private Social flag = Social.Local;

    // createdAt: INSERT 시 자동 저장
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // deletedAt: NULL 가능, 필요할 때 값 설정
    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP NULL")
    private LocalDateTime deletedAt;

    @ColumnDefault("false")  // 'false'가 기본값
    private boolean deletedFlag = false; // Java에서도 기본값 설정
}
