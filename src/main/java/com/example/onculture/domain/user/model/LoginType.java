package com.example.onculture.domain.user.model;

public enum LoginType {
    LOCAL_ONLY,    // 로컬 로그인만 한 이메일
    SOCIAL_ONLY,   // 소셜 로그인만 한 이메일
    BOTH           // 소셜 + 로컬 로그인 모두 가능한 이메일
}
