package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;

import java.util.List;
import java.util.Optional;

public interface AiDomainService {
    AiResultResponseDto triggerAnalysis(Long userId);
    Optional<AiResultResponseDto> getLatestResult(Long userId);
}
