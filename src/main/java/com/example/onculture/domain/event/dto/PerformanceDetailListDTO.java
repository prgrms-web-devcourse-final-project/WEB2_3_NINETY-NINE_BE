package com.example.onculture.domain.event.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "dbs") // ✅ 최상위 XML 태그 매핑
public class PerformanceDetailListDTO {
    @JacksonXmlElementWrapper(useWrapping = false) // ✅ <dbs> 안의 <db> 리스트 매핑
    @JacksonXmlProperty(localName = "db")
    private List<PerformanceDetailDTO> dbList;
}




