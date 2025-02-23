package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.domain.Musical;
import com.example.onculture.domain.event.domain.Theater;
import com.example.onculture.domain.event.dto.PerformanceDTO;
import com.example.onculture.domain.event.dto.PerformanceDetailDTO;
import com.example.onculture.domain.event.dto.PerformanceDetailListDTO;
import com.example.onculture.domain.event.dto.PerformanceListDTO;
import com.example.onculture.domain.event.repository.MusicalRepository;
import com.example.onculture.domain.event.repository.TheaterRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KopisService {
    private final RestTemplate restTemplate;
    private final MusicalRepository musicalRepository;
    private final TheaterRepository theaterRepository;
    private final XmlMapper xmlMapper;
    private static final String BASE_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";
    private static final String SERVICE_KEY = "";

    public KopisService(
                        MusicalRepository musicalRepository,
                        TheaterRepository theaterRepository) {
        this.restTemplate = new RestTemplate();
        this.musicalRepository = musicalRepository;
        this.theaterRepository = theaterRepository;
        this.xmlMapper = new XmlMapper();
    }

    public void savePerformances(String from, String to, String genre) {
        String genreCode = getGenreCode(genre);

        // 1. 공연 목록 API 호출 (XML 응답)
        String performanceListResponse = restTemplate.getForObject(
                getPerformanceListUrl(from, to, genreCode), String.class);

        // 2. XML 파싱을 통해 PerformanceListDTO로 변환 후 공연 ID 목록 추출
        List<String> performanceIds;
        try {
            PerformanceListDTO listDTO = xmlMapper.readValue(new StringReader(performanceListResponse), PerformanceListDTO.class);
            performanceIds = listDTO.getPerformanceDTOS().stream()
                    .map(PerformanceDTO::getMt20id)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("XML Parsing Error: 공연 목록", e);
        }

        // 3. 각 공연 ID별 상세정보 API 호출 및 엔터티 변환
        List<Object> entities = new ArrayList<>();
        for (String id : performanceIds) {
            String detailResponse = restTemplate.getForObject(getPerformanceDetailUrl(id), String.class);
            try {
                PerformanceDetailListDTO detailListDTO = xmlMapper.readValue(new StringReader(detailResponse), PerformanceDetailListDTO.class);
                if (detailListDTO.getDbList() != null && !detailListDTO.getDbList().isEmpty()) {
                    for (PerformanceDetailDTO dto : detailListDTO.getDbList()) {
                        if ("musical".equalsIgnoreCase(genre)) {
                            entities.add(dto.convertToMusical());
                        } else if ("theater".equalsIgnoreCase(genre)) {
                            entities.add(dto.convertToTheater());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("XML Parsing Error: 공연 상세 정보 for id: " + id, e);
            }
        }

        // 4. 변환된 엔터티 저장 (장르에 따라 분기)
        if (!entities.isEmpty()) {
            if ("musical".equalsIgnoreCase(genre)) {
                List<Musical> musicals = entities.stream()
                        .map(entity -> (Musical) entity)
                        .collect(Collectors.toList());
                musicalRepository.saveAll(musicals);
            } else if ("theater".equalsIgnoreCase(genre)) {
                List<Theater> theaters = entities.stream()
                        .map(entity -> (Theater) entity)
                        .collect(Collectors.toList());
                theaterRepository.saveAll(theaters);
            } else {
                throw new CustomException(ErrorCode.INVALID_GENRE_REQUEST);
            }
        }

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

    private String getPerformanceListUrl(String from, String to, String genreCode) {
        URI uri = URI.create(BASE_URL);
        return UriComponentsBuilder.fromUri(uri)
                .queryParam("service", SERVICE_KEY)
                .queryParam("stdate", from)
                .queryParam("eddate", to)
                .queryParam("rows", 10)
                .queryParam("cpage", 1)
                .queryParam("shcate", genreCode)
                .toUriString();
    }

    private String getPerformanceDetailUrl(String performanceId) {
        URI uri = URI.create(BASE_URL + "/" + performanceId);
        return UriComponentsBuilder.fromUri(uri)
                .queryParam("service", SERVICE_KEY)
                .toUriString();
    }
}
