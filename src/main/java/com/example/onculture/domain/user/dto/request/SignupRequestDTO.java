package com.example.onculture.domain.user.dto.request;

import lombok.*;

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
