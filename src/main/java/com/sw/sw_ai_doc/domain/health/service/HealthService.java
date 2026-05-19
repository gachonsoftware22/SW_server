package com.sw.sw_ai_doc.domain.health.service;

import com.sw.sw_ai_doc.domain.health.dto.HealthRequestDto;
import com.sw.sw_ai_doc.domain.health.dto.HealthResponseDto;

import java.util.List;

public interface HealthService {
    HealthResponseDto createHealth(Long userId, HealthRequestDto dto);
    List<HealthResponseDto> getHealthList(Long userId);
    HealthResponseDto getHealthDetail(Long healthId, Long userId);
    HealthResponseDto updateHealth(Long healthId, Long userId, HealthRequestDto dto);
    void deleteHealth(Long healthId, Long userId);
}
