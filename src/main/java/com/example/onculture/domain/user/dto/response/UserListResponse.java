package com.example.onculture.domain.user.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {

    private Long id;
    private String email;
}
