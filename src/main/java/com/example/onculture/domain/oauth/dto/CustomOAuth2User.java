package com.example.onculture.domain.oauth.dto;

import com.example.onculture.domain.user.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String provider;
    private final String email;
    private final String nickname;
    private final Role role;


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        // 권한 여러개 가질 때
        /*
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> role);
        return collection;
         */
    }

    @Override
    public String getName() {
        return nickname;
    }
}
