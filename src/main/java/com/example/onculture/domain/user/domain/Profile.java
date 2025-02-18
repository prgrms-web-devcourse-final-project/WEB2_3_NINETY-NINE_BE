package com.example.onculture.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "user_id", nullable = false)    // FK 매핑
    @OnDelete(action = OnDeleteAction.CASCADE)      // 단방향 때에도, 해당 유저가 삭제되면 게시물이 자동 삭제되게 설정
    private User user;

    private String description;

    @Column(name = "profile_image")
    private String profileImage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)  // enum 값을 DB에 문자열로 저장
    @Column(name = "interests")
    private List<Interest> interests;
}
