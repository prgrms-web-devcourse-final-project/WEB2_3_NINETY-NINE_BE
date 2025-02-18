package com.example.onculture.domain.user.dto.request;

import com.example.onculture.domain.user.domain.Gender;
import com.example.onculture.domain.user.domain.Interest;
import com.example.onculture.domain.user.domain.Social;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    private String email;
    private String password;
    private String nickname;
}
