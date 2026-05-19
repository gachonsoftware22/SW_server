package com.sw.sw_ai_doc.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) return new ArrayList<>();
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON 역변환 실패", e);
        }
    }
}
