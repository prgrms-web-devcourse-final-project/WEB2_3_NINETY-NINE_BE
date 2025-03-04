package com.example.onculture.domain.event.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "dbs") // XML 루트 태그 매칭
public class PerformanceListDTO {

    @JacksonXmlElementWrapper(useWrapping = false) // <dbs> 안에 있는 리스트 <db> 매핑
    @JacksonXmlProperty(localName = "db")
    private List<PerformanceDTO> performanceDTOS;
}

