package com.sw.sw_ai_doc.domain.health.dto;

import com.sw.sw_ai_doc.domain.health.entity.Health;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class HealthResponseDto {
    private Long healthId;
    private Long userId;
    private String symptom;
    private String history;
    private String note;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HealthResponseDto from(Health health) {
        return HealthResponseDto.builder()
                .healthId(health.getHealthId())
                .userId(health.getUserId())
                .symptom(health.getSymptom())
                .history(health.getHistory())
                .note(health.getNote())
                .recordDate(health.getRecordDate())
                .createdAt(health.getCreatedAt())
                .updatedAt(health.getUpdatedAt())
                .build();
    }
}
