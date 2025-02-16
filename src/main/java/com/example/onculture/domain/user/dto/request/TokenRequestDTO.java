package com.example.onculture.domain.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenRequestDTO {

    private String accessToken;
    private String refreshToken;
}
