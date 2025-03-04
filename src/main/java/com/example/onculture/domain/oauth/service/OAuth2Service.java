package com.example.onculture.domain.oauth.service;


import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

// 프론트에서 인증 코드 받을 때 사용하는 클래스
/*
@Slf4j
@Getter
@Service
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;

    private String kakaoClientId;
    private String kakaoClientSecret;
    private String kakaoRedirectUri;
    private String googleClientId;
    private String googleClientSecret;
    private String googleRedirectUri;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public OAuth2Service(
            @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String kakaoClientId,
            @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String kakaoClientSecret,
            @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String kakaoRedirectUri,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String googleClientSecret,
            @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") String googleRedirectUri
    ) {
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.kakaoRedirectUri = kakaoRedirectUri;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleRedirectUri = googleRedirectUri;
    }

    // 인가 코드를 받아서 accessToken을 반환
    public String getAccessToken(String code, String provider){
        if ("kakao".equalsIgnoreCase(provider)) {
            return getKakaoAccessToken(code);
        } else if ("google".equalsIgnoreCase(provider)) {
            return getGoogleAccessToken(code);
        } else {
            throw new IllegalArgumentException("지원하지 않는 제공자입니다.");
        }
    }

    // ✅ Kakao 인가 코드로 accessToken 요청
    private String getKakaoAccessToken(String code) {
        String accessToken = "";
        String refreshToken = "";
        String reqUrl = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 필수 헤더 세팅
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();

            // 필수 쿼리 파라미터 세팅
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(kakaoClientId);
            sb.append("&redirect_uri=").append(kakaoRedirectUri);
            sb.append("&code=").append(code);

            log.info("Kakao 요청 파라미터: {}", sb.toString());

            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.getAccessToken] responseCode = {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseSb.append(line);
            }
            String result = responseSb.toString();
            log.info("Kakao 응답: {}", result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            br.close();
            bw.close();
        } catch (Exception e) {
            log.error("Kakao 액세스 토큰 요청 오류: ", e);
        }
        return accessToken;
    }

    // ✅ Google 인가 코드로 accessToken 요청
    private String getGoogleAccessToken(String code) {
        String accessToken = "";
        String reqUrl = "https://oauth2.googleapis.com/token";

        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 필수 헤더 세팅
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();

            // 필수 쿼리 파라미터 세팅
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(googleClientId);
            sb.append("&client_secret=").append(googleClientSecret);
            sb.append("&redirect_uri=").append(googleRedirectUri);
            sb.append("&code=").append(code);

            log.info("Google 요청 파라미터: {}", sb.toString());

            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();
            log.info("[GoogleApi.getAccessToken] responseCode = {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseSb.append(line);
            }
            String result = responseSb.toString();
            log.info("Google 응답: {}", result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            accessToken = element.getAsJsonObject().get("access_token").getAsString();

            br.close();
            bw.close();
        } catch (Exception e) {
            log.error("Google 액세스 토큰 요청 오류: ", e.getMessage());
        }
        return accessToken;
    }

    public HashMap<String, Object> getUserInfo(String accessToken, String provider) {
        HashMap<String, Object> userInfo = new HashMap<>();

        // Kakao와 Google의 API URL 다르게 설정
        String reqUrl;
        if ("kakao".equalsIgnoreCase(provider)) {
            reqUrl = "https://kapi.kakao.com/v2/user/me";  // Kakao API endpoint
        } else if ("google".equalsIgnoreCase(provider)) {
            reqUrl = "https://www.googleapis.com/oauth2/v3/userinfo";  // Google API endpoint
        } else {
            throw new IllegalArgumentException("지원하지 않는 제공자입니다.");
        }

        try {
            // URL 연결
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // GET 요청 방식과 Authorization 헤더 추가
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);  // 액세스 토큰을 Authorization 헤더로 추가
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // 응답 코드 처리
            int responseCode = conn.getResponseCode();
            log.info("[OAuth2Api.getUserInfo] responseCode : {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line;
            StringBuilder responseSb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseSb.append(line);
            }
            String result = responseSb.toString();
            log.info("responseBody = {}", result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            // 제공자별로 응답 포맷이 다르므로 처리 방식 구분
            if ("kakao".equalsIgnoreCase(provider)) {
                // Kakao의 경우, nickname과 email을 properties와 kakao_account에서 추출
                JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
                JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

                String nickname = properties.getAsJsonObject().get("nickname").getAsString();
                String email = kakaoAccount.getAsJsonObject().get("email").getAsString();

                userInfo.put("nickname", nickname);
                userInfo.put("email", email);
            } else if ("google".equalsIgnoreCase(provider)) {
                // Google의 경우, nickname과 email을 JSON 객체에서 추출
                String email = element.getAsJsonObject().get("email").getAsString();
                String name = element.getAsJsonObject().get("name").getAsString();

                userInfo.put("nickname", name); // Google에서는 nickname 대신 name 사용
                userInfo.put("email", email);
            }

            br.close();
        } catch (Exception e) {
            log.error("사용자 정보 조회 오류: ", e.getMessage());
        }

        return userInfo;
    }
}
 */
