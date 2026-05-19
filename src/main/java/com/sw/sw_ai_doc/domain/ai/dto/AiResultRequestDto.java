package com.sw.sw_ai_doc.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiResultRequestDto {
    private Long userId;
    private String healthCsv;
    private String prescriptionCsv;
}
