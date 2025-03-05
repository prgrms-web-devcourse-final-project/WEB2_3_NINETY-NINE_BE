package com.example.onculture.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
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

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // ResponseCookie를 사용하여 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")               // 전체 도메인에서 사용 가능하도록 설정
                .sameSite("None")        // SameSite=None을 명시적으로 설정하여 크로스사이트 요청 허용
                .httpOnly(false)        // 액세스 토큰용이므로 HttpOnly 설정하지 않음 (프론트엔드에서 쿠키를 접근 가능하게 설정)
                .secure(false)           // HTTP 환경에서는 Secure=false로 설정
                .maxAge(maxAge)          // 쿠키 유효 시간 설정 (초 단위)
                .build();

        // 쿠키를 응답에 추가
        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 리프레시 전용 쿠키 생성 메서드 (HttpOnly 설정)
    public static void addSecurityCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // ResponseCookie를 사용하여 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")               // 전체 도메인에서 사용 가능하도록 설정
                .sameSite("None")        // SameSite=None을 명시적으로 설정하여 크로스사이트 요청 허용
                .httpOnly(true)         // HttpOnly 설정 (프론트엔드에서 쿠키를 읽지 못하도록)
                .secure(false)           // HTTP 환경에서는 Secure=false로 설정
                .maxAge(maxAge)          // 쿠키 유효 시간 설정 (초 단위)
                .build();

        // 쿠키를 응답에 추가
        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                // ResponseCookie로 쿠키 삭제 설정
                ResponseCookie cookieToDelete = ResponseCookie.from(name, "")  // 쿠키 값은 빈 문자열로 설정
                        .path("/")                  // 쿠키의 경로는 그대로 유지
                        .sameSite("None")           // SameSite=None 설정 (Cross-Site 요청 허용)
                        .secure(false)              // HTTP 환경에서만 사용
                        .maxAge(0)                  // 유효 시간을 0으로 설정하여 만료 처리
                        .build();

                // 응답에 쿠키 삭제를 반영
                response.addHeader("Set-Cookie", cookieToDelete.toString());
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

    // 개선 전
    /*
    // 요청값(이름, 값, 만료 기간)을 바탕으로 쿠키 생성 메서드
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");       // 전체 도메인에서 사용 가능하도록 설정
        cookie.setSecure(false);   // HTTP 환경에서는 Secure=false로 설정
        cookie.setMaxAge(maxAge);  // 쿠키 유효 시간 설정 (초 단위)

        // SameSite=None을 명시적으로 설정하여 크로스사이트 요청 허용
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);

        // 쿠키 헤더에도 SameSite=None 적용 (브라우저가 SameSite 속성을 인식하도록)
        response.addHeader("Set-Cookie", String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None",
                name, value, maxAge));
    }

    // 리프레시 전용 쿠키 생성 메서드 (HttpOnly 설정)
    public static void addSecurityCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);   // XSS 공격 방지를 위해 HttpOnly 설정
        cookie.setSecure(false);    // HTTP 환경에서는 Secure=false로 설정
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "None");  // Cross-Site 요청 허용
        response.addCookie(cookie);

        // 쿠키 헤더에도 SameSite=None 적용
        response.addHeader("Set-Cookie", String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None",
                name, value, maxAge));
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if ( cookies == null ) return;

        for ( Cookie cookie : cookies ) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setSecure(false);        // HTTP 에서만 전송 (HTTPS일 경우 true로 변경)
                cookie.setMaxAge(0);
                cookie.setAttribute("SameSite", "None");        // Cross-Site 요청 허용
                response.addCookie(cookie);
            }
        }
    }
     */


}
