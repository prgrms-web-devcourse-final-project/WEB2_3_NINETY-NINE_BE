package com.example.onculture.domain.oauth.Info;

import java.util.Map;

// 카카오 Info 데이터 처리 클래스
public class KaKaoOAuth2UserInfo extends OAuth2UserInfo {

    public KaKaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? kakaoAccount.get("email").toString() : null;
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return "Unknown";
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile != null && profile.containsKey("nickname")) {
            return (String) profile.get("nickname");
        }

        // profile이 없으면 properties에서 nickname 가져오기
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null && properties.containsKey("nickname")) {
            return (String) properties.get("nickname");
        }

        return "Unknown";
    }

    @Override
    public String getProfileImage() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) return null;
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile != null ? (String) profile.get("thumbnail_image_url") : null;
    }
}
