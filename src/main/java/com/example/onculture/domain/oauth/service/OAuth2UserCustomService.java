package com.example.onculture.domain.oauth.service;

import com.example.onculture.domain.oauth.Info.OAuth2UserInfo;
import com.example.onculture.domain.oauth.Info.OAuth2UserInfoFactory;
import com.example.onculture.domain.oauth.dto.CustomOAuth2User;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 현재 단계에 OAuth2UserRequest userRequest에는 액세스 토큰이 담겨 있음
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        User saveUser = saveOrUpdate(user, userRequest);

        // 기본 OAuth2User 객체를 그대로 사용하면 User 엔티티와 연결하기 어렵기 때문에
        // 커스텀 사용자 객체 (CustomOAuth2User) 를 만들어서 원하는 필드를 추가
        return CustomOAuth2User.builder()
                .attributes(user.getAttributes())
                .provider(userRequest.getClientRegistration().getRegistrationId().toUpperCase())
                .email(saveUser.getEmail())
                .nickname(saveUser.getNickname())
                .role(saveUser.getRole())
                .build();
    }

    // 로컬 회원가입 기록이 있는 유저면 업데이트, 없으면 유저 생성
    private User saveOrUpdate(OAuth2User oauth2User, OAuth2UserRequest userRequest) {
        // 픒랫폼 맞춤형 OAuth2UserInfo 객체 생성 ( 인자: 플랫폼명, 플랫폼에서 반환된 Info 데이터 )
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                userRequest.getClientRegistration().getRegistrationId(),
                oauth2User.getAttributes());

        // 픒랫폼 맞춤형 OAuth2UserInfo 객체에서 필요 데이터 불러오기
        String email = userInfo.getEmail();
        String name = userInfo.getName();
        Social socialType = Social.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        System.out.println("email: " + email);
        System.out.println("name: " + name);
        System.out.println("socialType: " + socialType);

        // `orElseGet()`을 사용하여 기존 유저 조회 후 업데이트
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {      // 기존 유저일 경우
                    existingUser.socialUpdate(socialType, existingUser.getSocials());
                    return existingUser;
                })
                .orElseGet(() -> User.builder()     // 기존 유저가 아닐 경우
                        .email(email)
                        .nickname(name)
                        .role(Role.USER)
                        .loginType(LoginType.SOCIAL_ONLY)
                        .socials(new HashSet<>(Set.of(socialType)))
                        .build());

        return userRepository.save(user);
    }
}
