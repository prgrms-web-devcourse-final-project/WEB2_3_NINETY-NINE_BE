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

// 인가 요청을 쿠키에 저장하고 관리하는 이유
/*
1. 무상태(Stateless) 인증 지원 ( 클라이언트가 상태 유지, 서버는 안 함 )
2. OAuth2 로그인 과정에서 요청 정보를 유지 ( 리디렉션 과정에서 인가 요청 정보가 삭제되는 것을 방지 )
3. 다중 서버 환경(로드 밸런싱)에서 세션 동기화 문제 방지
( 여러 서버에서 동일한 OAuth2 로그인 요청을 처리 / 세션은 서버마다 개별로 존재하기에 동일한 인가 정보가 없을 수 있음 )
4. 브라우저 기반 OAuth2 로그인에서 세션 유지가 어려울 때
5. 서버 리소스를 절약하기 위해
 */

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
