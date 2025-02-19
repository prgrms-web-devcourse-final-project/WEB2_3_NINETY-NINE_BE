package com.example.onculture.domain.user.dto.response;

import com.example.onculture.domain.user.domain.Gender;
import com.example.onculture.domain.user.domain.Interest;
import com.example.onculture.domain.user.domain.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSimpleResponse {

    private String email;
    private String nickname;
}
