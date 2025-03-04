package com.example.onculture.domain.event.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class KakaoAddressService {

    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoApiKey;

    private final String baseUrl = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KakaoAddressService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 검색어를 전처리합니다.
     * 슬래시를 공백으로 대체하고, 연속된 공백을 하나로 줄입니다.
     */
    private String normalizeQuery(String query) {
        return query.replace("/", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * 주어진 건물명을 기반으로 카카오 키워드 검색 API를 호출하여
     * 첫 번째 결과의 도로명 주소(road_address_name) 또는 지번 주소(address_name)를 반환합니다.
     *
     * @param buildingName 검색할 건물명
     * @return 검색된 주소 (검색 결과가 없으면 null)
     */
    public String getAddressFromBuildingName(String buildingName) {
        try {
            // 검색어 전처리: 슬래시 제거, 연속 공백 축소
            buildingName = normalizeQuery(buildingName);
            // API의 쿼리 길이 제한(100자)을 초과하면 자르기
            if (buildingName.length() > 1000) {
                buildingName = buildingName.substring(0, 1000);
            }
            // URL 인코딩
            String encodedQuery = URLEncoder.encode(buildingName, StandardCharsets.UTF_8);
            // analyze_type 파라미터 추가
            String requestUrl = baseUrl + "?query=" + buildingName + "&analyze_type=similar";
            System.out.println("Request URL: " + requestUrl);

            // HTTP 헤더에 KakaoAK 키를 추가합니다.
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Kakao API 호출
            // RestTemplate을 통해 API 호출
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Response Body: " + response.getBody());
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode documents = root.path("documents");
                if (documents.isArray() && !documents.isEmpty()) {
                    JsonNode firstResult = documents.get(0);
                    // 우선 도로명 주소를 추출, 없으면 지번 주소 추출
                    String roadAddress = firstResult.path("road_address_name").asText();
                    if (roadAddress != null && !roadAddress.isEmpty()) {
                        return roadAddress;
                    }
                    return firstResult.path("address_name").asText();
                } else {
                    System.out.println("검색 결과가 없습니다: " + buildingName);
                }
            } else {
                System.out.println("API 호출 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
