package com.example.onculture.domain.user.dto.response;

import com.example.onculture.domain.user.model.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSimpleResponse {

    private String email;
    private String nickname;
    private Role role;
}
