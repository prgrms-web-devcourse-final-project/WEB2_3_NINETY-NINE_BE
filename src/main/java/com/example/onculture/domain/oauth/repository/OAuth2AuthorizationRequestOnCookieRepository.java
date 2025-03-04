package com.example.onculture.domain.oauth.repository;

import com.example.onculture.global.utils.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import static com.example.onculture.global.utils.CookieUtil.COOKIE_EXPIRE_SECONDS;
import static com.example.onculture.global.utils.CookieUtil.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME;

// OAuth2 로그인 과정에서 인가 요청(Authorization Request)을 쿠키에 저장하고 관리하는 저장소
// AuthorizationRequestRepository<OAuth2AuthorizationRequest> : // OAuth2 인증 요청을 저장/조회/삭제하는 기능 제공
@RequiredArgsConstructor
@Component
public class OAuth2AuthorizationRequestOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // OAuth2 인가 요청 저장 메서드( 인가 요청 단계에서 실행 )
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {

        // authorizationRequest가 null이면 쿠키 삭제
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        // OAuth2 인가 요청을 직렬화(serialize) 후 쿠키에 저장
        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    }

    // 쿠키에 있는 OAuth2 인가 요청 조회 메서드( 액세스 요청 단계에서 실행 )
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

        // 요청에서 쿠키 가져오기
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie == null) {
            return null; // 쿠키가 없으면 null 반환
        }

        // 가져온 쿠키를 OAuth2AuthorizationRequest 객체로 변환(역직렬화)해서 반환
        return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
    }

    // OAuth2 인가 요청 삭제 메서드( 액세스 토큰 발급 후 실행 )
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        // 운영 환경에서는 해당 코드 사용
        /*
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response); // 쿠키 삭제 추가
        return authorizationRequest;
         */

        // 테스트를 위해 삭제 x , 쿠키에서 요청 읽어오기
        return this.loadAuthorizationRequest(request);
    }

    // 쿠키에 있는 OAuth2 인가 요청 내용을 삭제하는 메서드
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
