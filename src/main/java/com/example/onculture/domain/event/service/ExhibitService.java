package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.dto.*;
import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;

    private final BookmarkRepository bookmarkRepository;
    // application.properties에 설정된 인코딩된 서비스키
    @Value("${PUBLIC_API_SERVICE_KEY}")
    private String serviceKey;

    // 공연/전시 상세정보 조회
    public EventResponseDTO getExhibitDetail(Long seq, Long userId) {
        EventResponseDTO eventResponseDTO = exhibitRepository.findById(seq)
                .map(exhibitEntity -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndPerformanceId(userId, exhibitEntity.getSeq())
                                    .isPresent();
                    return new EventResponseDTO(exhibitEntity, isBookmarked);
                })
                .orElseThrow(() -> new RuntimeException("Performance not found with seq: " + seq));
        return eventResponseDTO;
    }

    // 클라이언트로부터 전달받은 공공데이터(JSON)를 DB에 저장하는 메서드
    public void savePublicData(PublicDataRequestDTO requestDTO) {
        java.sql.Date startDate = null;
        java.sql.Date endDate = null;

        try {
            String start = requestDTO.getStartDate();
            String end = requestDTO.getEndDate();

            // "yyyyMMdd" 형식 -> "yyyy-MM-dd" 형식으로 변환
            if (start != null && start.length() == 8) {
                String formattedStart = start.substring(0, 4) + "-" + start.substring(4, 6) + "-" + start.substring(6, 8);
                startDate = java.sql.Date.valueOf(formattedStart);
            }
            if (end != null && end.length() == 8) {
                String formattedEnd = end.substring(0, 4) + "-" + end.substring(4, 6) + "-" + end.substring(6, 8);
                endDate = java.sql.Date.valueOf(formattedEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 파싱 실패 시 기본값 또는 예외 처리 로직 추가 가능
        }

        // determineStatus 메서드는 java.sql.Date 타입을 받음
        String status = determineStatus(startDate, endDate);

        ExhibitEntity entity = ExhibitEntity.builder()
                .title(requestDTO.getTitle())
                .startDate(startDate)
                .endDate(endDate)
                .place(requestDTO.getPlace())
                .realmName(requestDTO.getRealmName())
                .area(requestDTO.getArea())
                .thumbnail(requestDTO.getThumbnail())
                .gpsX(requestDTO.getGpsX())
                .gpsY(requestDTO.getGpsY())
                .exhibitStatus(status) // 계산된 상태 저장
                .build();
        exhibitRepository.save(entity);
    }


    /**
     * 공공데이터 API로부터 XML을 받아 JSON으로 변환 후, DB에 저장하는 로직 (페이지네이션 적용)
     */
    public void fetchXmlAndSaveWithPagination() {
        int pageNo = 1;
        int rows = 10; // 한 페이지당 반환되는 데이터 수
        boolean hasMoreData = true;

        while (hasMoreData) {
            // API 호출 URL 생성 (PageNo 파라미터 포함)
            String apiUrl = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B553457/nopenapi/rest/publicperformancedisplays/period")
                    .queryParam("serviceKey", serviceKey)  // 인코딩된 서비스키 그대로 사용
                    .queryParam("from", "20250301")
                    .queryParam("to", "20250401")
                    .queryParam("rows", rows)
                    .queryParam("PageNo", pageNo)
                    .build()//추가 인코딩 방지
                    .toUriString();

            System.out.println("Final API URL: " + apiUrl);


            HttpURLConnection conn = null;
            BufferedReader rd = null;
            try {
                URL url = new URL(apiUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/xml");

                // 응답 코드에 따라 스트림 선택 (UTF-8 인코딩 적용) 한글 깨짐 이슈
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    System.out.println("Response code: " + conn.getResponseCode());
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();
                String responseXml = sb.toString();
                System.out.println("Page " + pageNo + " - Received XML: " + responseXml);

                // XML → JSON 변환
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode rootNode = xmlMapper.readTree(responseXml);
                System.out.println("Page " + pageNo + " - Converted JSON: " + rootNode.toPrettyString());

                // 실제 데이터는 "body" > "items" > "item" 배열에 있음
                JsonNode itemArray = rootNode.path("body").path("items").path("item");

                if (itemArray.isArray() && itemArray.size() > 0) {
                    // 각 페이지의 데이터를 DB에 저장
                    for (JsonNode itemNode : itemArray) {
                        PublicDataRequestDTO dto = PublicDataRequestDTO.builder()
                                .title(itemNode.path("title").asText())
                                .startDate(itemNode.path("startDate").asText())
                                .endDate(itemNode.path("endDate").asText())
                                .place(itemNode.path("place").asText())
                                .realmName(itemNode.path("realmName").asText())
                                .area(itemNode.path("area").asText())
                                .thumbnail(itemNode.path("thumbnail").asText())
                                .gpsX(itemNode.path("gpsX").isMissingNode() ? null : itemNode.path("gpsX").asDouble())
                                .gpsY(itemNode.path("gpsY").isMissingNode() ? null : itemNode.path("gpsY").asDouble())
                                .build();
                        savePublicData(dto);
                        System.out.println("저장한 데이터: " + dto);
                    }
                    // 반환된 데이터 건수가 rows 미만이면 마지막 페이지로 판단
                    if (itemArray.size() < rows) {
                        hasMoreData = false;
                        System.out.println("더 이상 데이터가 없습니다. 총 " + pageNo + " 페이지 처리 완료.");
                    } else {
                        pageNo++; // 다음 페이지 처리
                    }
                } else {
                    System.out.println("데이터가 없습니다. 총 " + (pageNo - 1) + " 페이지 처리 완료.");
                    hasMoreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                hasMoreData = false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }


    // 공연 상태 결정 로직: 현재 날짜와 시작/종료일 비교
    private String determineStatus(java.sql.Date startDate, java.sql.Date endDate) {
        java.util.Date today = new java.util.Date();
        if (endDate != null && today.after(endDate)) {
            return "진행 종료";
        } else if (startDate != null && today.before(startDate)) {
            return "진행 예정";
        } else if (startDate != null && endDate != null && (!today.before(startDate) && !today.after(endDate))) {
            return "진행중";
        }
        return "상태 미정";
    }

    //랜덤조회
    public List<EventResponseDTO> getRandomExhibitions(int randomSize,Long userId) {
        if (randomSize < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return exhibitRepository.findRandomExhibitions(randomSize)
                .stream()
                .map(exhibitEntity -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndExhibitEntitySeq(userId, exhibitEntity.getSeq())
                                    .isPresent();
                    return new EventResponseDTO(exhibitEntity, isBookmarked);
                })
                .toList();
    }

    public EventPageResponseDTO searchExhibits(String region, String status, String titleKeyword, int pageNum, int pageSize, Long userId) {
        Specification<ExhibitEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 제목 키워드 검색 (대소문자 구분 없이)
            if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
                Expression<String> titleExpression = root.get("title").as(String.class);
                Expression<String> lowerTitle = criteriaBuilder.lower(titleExpression);

                predicates.add(
                        criteriaBuilder.like(
                                lowerTitle,
                                "%" + titleKeyword.toLowerCase() + "%"
                        )
                );
            }

            // 지역 필터
            if (region != null && !region.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("area"), region));
            }

            // 공연 상태(공연중, 공연예정) 필터
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("exhibitStatus"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<ExhibitEntity> exhibitPage = exhibitRepository.findAll(spec, pageable);

        List<EventResponseDTO> posts = exhibitPage.getContent()
                .stream()
                .map(exhibitEntity -> {
                    boolean isBookmarked = userId != null &&
                            bookmarkRepository.findByUserIdAndPerformanceId(userId, exhibitEntity.getSeq())
                                    .isPresent();
                    return new EventResponseDTO(exhibitEntity, isBookmarked);
                })
                .toList();

        EventPageResponseDTO response = new EventPageResponseDTO();
        response.setPosts(posts);
        response.setTotalPages(exhibitPage.getTotalPages());
        response.setTotalElements(exhibitPage.getTotalElements());
        response.setPageNum(exhibitPage.getNumber());
        response.setPageSize(exhibitPage.getSize());
        response.setNumberOfElements(exhibitPage.getNumberOfElements());

        return response;
    }
}
