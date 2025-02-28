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
    private static final String SERVICE_KEY = "5c98e680b5fc421693f8a6b32bf04d9f";

    public KopisService(MusicalRepository musicalRepository, TheaterRepository theaterRepository) {
        this.restTemplate = new RestTemplate();
        this.musicalRepository = musicalRepository;
        this.theaterRepository = theaterRepository;
        this.xmlMapper = new XmlMapper();
    }

    public void savePerformances(String from, String to, String genre, String status) {
        // 장르 코드 변환
        String genreCode = getGenreCode(genre);

        // 공연 목록 API 호출 및 공연 ID 목록 추출
        List<String> performanceIds = fetchPerformanceIds(from, to, genreCode, status);

        // 각 공연 ID별 상세정보 API 호출 및 엔터티 변환
        List<Object> entities = new ArrayList<>();
        for (String id : performanceIds) {
            entities.addAll(fetchPerformanceEntities(id));
        }

        // 변환된 엔터티 저장 (장르에 따라 분기)
        saveEntities(entities, genre);
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

    private List<Object> fetchPerformanceEntities(String performanceId) {
        String detailResponse = restTemplate.getForObject(getPerformanceDetailUrl(performanceId), String.class);
        List<Object> entityList = new ArrayList<>();
        try {
            PerformanceDetailListDTO detailListDTO = xmlMapper.readValue(new StringReader(detailResponse), PerformanceDetailListDTO.class);
            if (detailListDTO.getDbList() != null && !detailListDTO.getDbList().isEmpty()) {
                for (PerformanceDetailDTO dto : detailListDTO.getDbList()) {
                    if (dto.getGenre().equalsIgnoreCase("뮤지컬")) {
                        entityList.add(dto.convertToMusical());
                    } else if (dto.getGenre().equalsIgnoreCase("연극")) {
                        entityList.add(dto.convertToTheater());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("XML Parsing Error: 공연 상세 정보 for id: " + performanceId, e);
        }
        return entityList;
    }

    private void saveEntities(List<Object> entities, String genre) {
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
                throw new CustomException(ErrorCode.SAVE_FAILED);
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
