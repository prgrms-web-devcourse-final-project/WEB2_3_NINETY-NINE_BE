package com.example.onculture.domain.event.util;

public class RegionMapper {

    /**
     * 크롤링한 주소 문자열을 미리 정의한 지역 카테고리로 매핑합니다.
     * 키워드에 따라 매핑하며, 명확하지 않은 경우 "전체"로 반환합니다.
     */
    public static String mapRegion(String address) {
        if (address == null || address.isEmpty()) {
            return "전체";
        }
        // 국내 주요 도시 및 광역시 매핑
        if (address.contains("서울")) {
            return "서울특별시";
        } else if (address.contains("인천")) {
            return "인천광역시";
        } else if (address.contains("대전")) {
            return "대전광역시";
        } else if (address.contains("광주")) {
            return "광주광역시";
        } else if (address.contains("대구")) {
            return "대구광역시";
        } else if (address.contains("울산")) {
            return "울산광역시";
        } else if (address.contains("부산")) {
            return "부산광역시";
        } else if (address.contains("세종")) {
            return "세종특별자치시";
        } else if (address.contains("경기") || address.contains("고양") || address.contains("고양시")|| address.contains("일산") || address.contains("수원") || address.contains("수원시")) {
            return "경기도" ;
        }
        // 기타 지역 (예: 강원, 충청, 전라, 경상, 제주)
        if (address.contains("강원") || address.contains("철원")) {
            return "강원도";
        } else if (address.contains("충청북도") || address.contains("충청북") || address.contains("충북") || address.contains("청주")) {
            return "충청북도"; // 필요에 따라 충청북/남을 세분화할 수 있음
        } else if (address.contains("전라북도")  || address.contains("전라북") || address.contains("군산")  || address.contains("익산") || address.contains("전북")) {
            return "전라북도"; // 마찬가지로 세분화 가능
        } else if (address.contains("전남") || address.contains("목포") || address.contains("담양")  || address.contains("여수")){
            return "전라남도";
        } else if (address.contains("창원") || address.contains("경남") || address.contains("김해") || address.contains("거제") || address.contains("진주"))  {
            return "경상남도";
        }else if (address.contains("경상북") || address.contains("경북")|| address.contains("영주") || address.contains("포항"))  {
            return "경상북도";
        } else if(address.contains("서천") || address.contains("마서")|| address.contains("아산") || address.contains("천안") || address.contains("천안시")|| address.contains("충청남")) {
            return "충청남도";
        } else if (address.contains("제주")) {
            return "제주특별자치도";
        }
        // 해외 또는 분류 불가의 경우 "전체"로 처리
        return "해외";
    }
}
