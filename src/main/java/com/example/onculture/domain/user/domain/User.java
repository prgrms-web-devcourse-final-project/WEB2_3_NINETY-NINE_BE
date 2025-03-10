package com.example.onculture.domain.user.domain;

import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    // 기본값 : Null
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "socials", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "socials")
    private Set<Social> socials = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // 양방향 연관 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @PrePersist
    public void prePersist() {
        this.loginType = this.loginType == null ? LoginType.LOCAL_ONLY : this.loginType;
        this.socials = this.socials == null || this.socials.isEmpty() ? new HashSet<>(Set.of(Social.LOCAL)) : this.socials;
        this.role = this.role == null ? Role.USER : this.role;
    }

    // 연관 관계 편의 메서드 추가 ( 사용 보류 )
//    public void setProfile(Profile profile) {
//        this.profile = profile;
//        profile.setUser(this);
//    }

    // 동일한 이메일로 2가지 이상의 로그인 방식을 사용할 경우
    public User socialUpdate(Social social, Set<Social> socials) {
        if (this.socials == null || this.socials.isEmpty()) {
            // socials가 null이거나 비어있을 경우 기본값으로 초기화
            this.socials = new HashSet<>(Set.of(Social.LOCAL));
        }

        if ( social == Social.LOCAL && !socials.contains(Social.GOOGLE) && !socials.contains(Social.KAKAO) ) {
            this.loginType = LoginType.LOCAL_ONLY;
        } else if (!socials.contains(Social.LOCAL) && socials.contains(Social.GOOGLE) || socials.contains(Social.KAKAO) ) {
            this.loginType = LoginType.SOCIAL_ONLY;
            this.socials.add(social);
        } else {
            this.loginType = LoginType.BOTH;
            this.socials.add(social);
        }
        return this;
    }
}
