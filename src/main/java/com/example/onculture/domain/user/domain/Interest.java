package com.example.onculture.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.value.qual.StringVal;

@Getter
@AllArgsConstructor
public enum Interest {

    P("팝업 스토어", "Pop-up Store"),
    E("전시회", "Exhibitions"),
    D("연극", "Drama"),
    M("뮤지컬", "Musicals"),
    F("페스티벌", "Festivals"),
    C("콘서트", "Concerts");

    private final String kor;
    private final String eng;
}
