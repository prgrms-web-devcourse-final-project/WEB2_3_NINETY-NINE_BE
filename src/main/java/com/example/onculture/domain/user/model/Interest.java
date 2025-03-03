package com.example.onculture.domain.user.model;

import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    // 한글명 -> ENUM 매핑을 위한 캐싱된 Map ( 시간될 때 정리하기 )
    private static final Map<String, Interest> KOR_TO_ENUM_MAP =
            // Interest.values() : 모든 ENUM을 배열로 반환
            // Arrays.stream() : 배열을 스트림으로 반환
            Arrays.stream(Interest.values())     // Interest 내 상수를 배열 전환 후 -> 스트림 형태로 변환
                    // .collect(Collectors.toMap()) : 스트림을 Map으로 반환
                    // toMap : 스트림의 요소를 키-값 쌍으로 맵핑하여 Map 생성
                    // Interest::getKor : Interest의 kor 필드를 키로 사용
                    // Function.identity() : 현재 스트림의 요소 그대로를 값으로 사용
                    .collect(Collectors.toMap(Interest::getKor, Function.identity()));

            // 이해를 위한 풀이
            /*
            Interest[] interests = Interest.values();
            Stream<Interest> stream = Arrays.stream(interests);
            Map<String, Interest> map = new HashMap<>();
            for (Interest interest : Interest.values()) {
                map.put(interest.getKor(), interest);
            }
             */


    // 한글명을 통해 ENUM 값 찾기
    public static Interest getInterestByKor(String kor) {
        Interest interest = KOR_TO_ENUM_MAP.get(kor);
        if (interest == null) throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        return interest;
    }
}
