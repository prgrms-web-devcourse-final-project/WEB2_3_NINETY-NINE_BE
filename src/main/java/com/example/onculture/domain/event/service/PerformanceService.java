package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.Performance;
import com.example.onculture.domain.event.dto.*;
import com.example.onculture.domain.event.repository.PerformanceRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceService {
    private final RestTemplate restTemplate;
    private final PerformanceRepository performanceRepository;
    private final XmlMapper xmlMapper;
    private static final String BASE_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";
    @Value("${KOPIS_SERVICE_KEY}")
    private String SERVICE_KEY;

    public PerformanceService(PerformanceRepository performanceRepository) {
        this.restTemplate = new RestTemplate();
        this.performanceRepository = performanceRepository;
        this.xmlMapper = new XmlMapper();
    }

    public void savePerformances(String from, String to, String genre, String status) {
        // 장르 코드 변환 (요청시 장르 코드를 따로 사용)
        String genreCode = getGenreCode(genre);

        // 공연 목록 API 호출 및 공연 ID 목록 추출
        List<String> performanceIds = fetchPerformanceIds(from, to, genreCode, status);

        // 각 공연 ID별 상세정보 API 호출 및 Performance 엔터티로 변환
        List<Performance> performanceList = new ArrayList<>();
        for (String id : performanceIds) {
            performanceList.addAll(fetchPerformanceEntities(id));
        }

        if (performanceList.isEmpty()) {
            throw new CustomException(ErrorCode.NO_CONTENT);
        }
        // Performance 엔터티 통합 저장
        performanceRepository.saveAll(performanceList);
    }

    public List<EventResponseDTO> getRandomPerformances(int randomSize) {
        if (randomSize < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return performanceRepository.findRandomPerformances(randomSize)
                .stream()
                .map(EventResponseDTO::new)
                .toList();
    }

    private List<String> fetchPerformanceIds(String from, String to, String genreCode, String status) {
        List<String> performanceIds = new ArrayList<>();
        int page = 1;
        while (true) {
            String performanceListResponse = restTemplate.getForObject(
                    getPerformanceListUrl(from, to, genreCode, page, status), String.class);
            try {
                PerformanceListDTO listDTO = xmlMapper.readValue(
                        new StringReader(performanceListResponse), PerformanceListDTO.class);
                if (listDTO.getPerformanceDTOS() == null || listDTO.getPerformanceDTOS().isEmpty()) {
                    break;
                }
                performanceIds.addAll(
                        listDTO.getPerformanceDTOS().stream()
                                .map(PerformanceDTO::getMt20id)
                                .collect(Collectors.toList()));
            } catch (Exception e) {
                throw new RuntimeException("XML Parsing Error: 공연 목록, page: " + page, e);
            }
            page++;
        }
        if (performanceIds.isEmpty()) {
            throw new CustomException(ErrorCode.NO_CONTENT);
        }
        return performanceIds;
    }

    private List<Performance> fetchPerformanceEntities(String performanceId) {
        String detailResponse = restTemplate.getForObject(getPerformanceDetailUrl(performanceId), String.class);
        List<Performance> performanceList = new ArrayList<>();
        try {
            PerformanceDetailListDTO detailListDTO = xmlMapper.readValue(new StringReader(detailResponse), PerformanceDetailListDTO.class);
            if (detailListDTO.getDbList() != null && !detailListDTO.getDbList().isEmpty()) {
                for (PerformanceDetailDTO dto : detailListDTO.getDbList()) {
                    performanceList.add(dto.convertToPerformance());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("XML Parsing Error: 공연 상세 정보 for id: " + performanceId, e);
        }
        return performanceList;
    }

    private String getGenreCode(String genre) {
        if ("musical".equalsIgnoreCase(genre)) {
            return "GGGA";
        } else if ("theater".equalsIgnoreCase(genre)) {
            return "AAAA";
        } else {
            throw new CustomException(ErrorCode.INVALID_GENRE_REQUEST);
        }
    }

    private String getPerformanceListUrl(String from, String to, String genreCode, int page, String status) {
        URI uri = URI.create(BASE_URL);
        return UriComponentsBuilder.fromUri(uri)
                .queryParam("service", SERVICE_KEY)
                .queryParam("stdate", from)
                .queryParam("eddate", to)
                .queryParam("rows", 10)
                .queryParam("cpage", page)
                .queryParam("shcate", genreCode)
                .queryParam("prfstate", status)
                .toUriString();
    }

    private String getPerformanceDetailUrl(String performanceId) {
        URI uri = URI.create(BASE_URL + "/" + performanceId);
        return UriComponentsBuilder.fromUri(uri)
                .queryParam("service", SERVICE_KEY)
                .toUriString();
    }
}
