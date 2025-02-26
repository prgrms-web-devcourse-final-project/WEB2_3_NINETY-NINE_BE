package com.example.onculture.domain.user.domain;

import com.example.onculture.domain.user.model.Interest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

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

    private String description;

    @Column(name = "profile_image")
    private String profileImage;

    // 기본값 : Null
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interests")
    private Set<Interest> interests = new HashSet<>();

    // 연관 관계 편의 메서드 추가 ( 사용 보류 )
//    public void setUser(User user) {
//        this.user = user;
//        user.setProfile(this);
//    }
}
