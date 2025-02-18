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
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Social flag;

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
