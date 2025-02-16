package com.example.onculture.domain.user.dto.response;

import com.example.onculture.domain.user.domain.Gender;
import com.example.onculture.domain.user.domain.Interest;
import com.example.onculture.domain.user.domain.Role;
import com.example.onculture.domain.user.domain.Social;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String email;

    private String nickname;

    private String description;

    private String birth;

    private Gender gender;

    private Role role;

    private LocalDateTime createdAt;

    private List<Interest> interests;
}
