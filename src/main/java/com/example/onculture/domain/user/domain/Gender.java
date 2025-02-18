package com.example.onculture.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    M("남자", "Male"),
    F("여자", "Female"),
    U("없음", "Unknow");

    private final String kor;
    private final String eng;
}
