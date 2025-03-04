package com.example.onculture.domain.oauth.Info;

import java.util.Map;

// 플랫폼별 Info 데이터 처리 클래스
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getEmail();
    public abstract String getName();
    public abstract String getProfileImage();
}
