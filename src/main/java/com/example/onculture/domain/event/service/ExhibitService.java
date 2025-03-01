package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.dto.ExhibitDTO;
import com.example.onculture.domain.event.dto.ExhibitDetailDTO;
import com.example.onculture.domain.event.dto.PublicDataRequestDTO;
import com.example.onculture.domain.event.domain.ExhibitEntity;
import com.example.onculture.domain.event.repository.ExhibitRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;

    // application.properties에 설정된 인코딩된 서비스키
    @Value("${public.api.serviceKey}")
    private String serviceKey;

    // 기간별 공연/전시 목록 조회
    public List<ExhibitDTO> getExhibitByPeriod(String from, String to) {
        List<ExhibitEntity> exhibit = exhibitRepository
                .findByStartDateGreaterThanEqualAndEndDateLessThanEqual(from, to);
        return exhibit.stream().map(this::toListDTO).collect(Collectors.toList());
    }

    // 지역별 공연/전시 목록 조회
    public List<ExhibitDTO> getExhibitByArea(String area, String from, String to) {
        List<ExhibitEntity> exhibit = exhibitRepository
                .findByAreaAndStartDateGreaterThanEqualAndEndDateLessThanEqual(area, from, to);
        return exhibit.stream().map(this::toListDTO).collect(Collectors.toList());
    }

    // 분야별 공연/전시 목록 조회
    public List<ExhibitDTO> getExhibitByRealm(String realmName, String from, String to) {
        List<ExhibitEntity> exhibit = exhibitRepository
                .findByRealmNameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(realmName, from, to);
        return exhibit.stream().map(this::toListDTO).collect(Collectors.toList());
    }

    // 공연/전시 상세정보 조회
    public ExhibitDetailDTO getExhibitDetail(Long seq) {
        ExhibitEntity exhibit = exhibitRepository.findById(seq)
                .orElseThrow(() -> new RuntimeException("Performance not found with seq: " + seq));
        return toDetailDTO(exhibit);
    }

    // 클라이언트로부터 전달받은 공공데이터(JSON)를 DB에 저장하는 메서드
    public void savePublicData(PublicDataRequestDTO requestDTO) {
        ExhibitEntity entity = ExhibitEntity.builder()
                .title(requestDTO.getTitle())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .place(requestDTO.getPlace())
                .realmName(requestDTO.getRealmName())
                .area(requestDTO.getArea())
                .thumbnail(requestDTO.getThumbnail())
                .gpsX(requestDTO.getGpsX())
                .gpsY(requestDTO.getGpsY())
                // 상세 정보 필드가 필요하면 여기서 추가할 수 있습니다.
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
                    .queryParam("from", "20250101")
                    .queryParam("to", "20250225")
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

    // Entity -> ListDTO 변환
    private ExhibitDTO toListDTO(ExhibitEntity p) {
        return ExhibitDTO.builder()
                .seq(p.getSeq())
                .title(p.getTitle())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .place(p.getPlace())
                .realmName(p.getRealmName())
                .area(p.getArea())
                .thumbnail(p.getThumbnail())
                .gpsX(p.getGpsX())
                .gpsY(p.getGpsY())
                .build();
    }

    // Entity -> DetailDTO 변환
    private ExhibitDetailDTO toDetailDTO(ExhibitEntity p) {
        return ExhibitDetailDTO.builder()
                .seq(p.getSeq())
                .title(p.getTitle())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .place(p.getPlace())
                .realmName(p.getRealmName())
                .area(p.getArea())
                .thumbnail(p.getThumbnail())
                .gpsX(p.getGpsX())
                .gpsY(p.getGpsY())
                .build();
    }
}
