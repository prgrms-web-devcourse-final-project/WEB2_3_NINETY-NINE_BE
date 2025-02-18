package com.example.onculture.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table( name = "profile" )
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id", updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "user_id", nullable = false )    // FK 매핑
    @OnDelete(action = OnDeleteAction.CASCADE)      // 해당 유저가 삭제되면 프로필도 자동 삭제되게 설정
    private User user;

    // LocalDate 사용 (시간 정보 제외)
    private LocalDate birth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String description;

    @Column(name = "profile_image")
    private String profileImage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interests")
    private List<Interest> interests;

    @PrePersist
    public void prePersist() {
        this.gender = this.gender == null ? Gender.U : this.gender;
    }
}
