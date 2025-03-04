package com.example.onculture.domain.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventPageResponseDTO {
    private List<EventResponseDTO> posts;
    private int totalPages;
    private long totalElements;
    private int pageNum;
    private int pageSize;
    private int numberOfElements;
}