package com.example.onculture.domain.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = refreshToken;
    }
}
