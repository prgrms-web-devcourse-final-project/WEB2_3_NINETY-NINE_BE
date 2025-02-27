package com.example.onculture.domain.user.dto.request;

import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SignupRequestDTO {

    private String email;
    private String password;
    private String nickname;
    private Role role = Role.USER;
    private LoginType loginType = LoginType.LOCAL_ONLY;
    private Set<Social> socials = new HashSet<>(Set.of(Social.LOCAL));
}
