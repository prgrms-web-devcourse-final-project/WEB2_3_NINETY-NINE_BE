package com.example.onculture.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.time.Duration;
import java.util.Base64;

// 나중에 정리 + user controller에도 적용
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";       // 액세스 코드 쿠키 이름
    public final static int ACCESS_TOKEN_COOKIE_DURATION = (int) Duration.ofDays(1).getSeconds(); // 86400초 (1일)
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";     // 리프레시 코드 쿠키 이름
    public final static int REFRESH_TOKEN_COOKIE_DURATION = (int) Duration.ofDays(14).getSeconds(); // 1209600초 (14일)
    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";    // OAuth2 인가 요청 쿠키 이름
    public final static int COOKIE_EXPIRE_SECONDS = 18000;      // OAuth2 인가 요청 쿠키의 유효 시간 ( 18000 = 5seconds )

    // 요청값(이름, 값, 만료 기간)을 바탕으로 쿠키 생성 메서드
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");       // 전체 도메인에서 사용 가능하도록 설정
        cookie.setMaxAge(maxAge);  // 쿠키 유효 시간 설정 (초 단위)
        response.addCookie(cookie);
    }

    // 리프레시 전용 쿠키 생성 메서드
    public static void addSecurityCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // XSS 공격 방지를 위해 HttpOnly 설정
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if ( cookies == null ) return;

        for ( Cookie cookie : cookies ) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    // 객체를 직렬화해 쿠키의 값으로 변환
    public static String serialize(Object object) {
        return Base64.getUrlEncoder() // URL-safe 방식으로 변경
                .encodeToString(SerializationUtils.serialize(object));
    }

    // 쿠키를 역직렬화해 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> clazz) {
        return clazz.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue()))
        );
    }
}
