package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;

public interface AiResultService {
    AiResultEntity analyze(AiResultRequestDto request);
}
