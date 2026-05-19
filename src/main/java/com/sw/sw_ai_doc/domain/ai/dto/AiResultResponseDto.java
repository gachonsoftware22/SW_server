package com.sw.sw_ai_doc.domain.ai.dto;

import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AiResultResponseDto {
    private Long resultId;
    private Long userId;
    private String healthStatus;
    private String summaryNote;
    private List<String> potentialDiseases;
    private List<String> recommendedFoods;
    private List<String> recommendedExercises;
    private String precautions;
    private LocalDateTime analysisDate;

    public static AiResultResponseDto from(AiResultEntity entity) {
        return AiResultResponseDto.builder()
                .resultId(entity.getResultId())
                .userId(entity.getUserId())
                .healthStatus(entity.getHealthStatus())
                .summaryNote(entity.getSummaryNote())
                .potentialDiseases(entity.getPotentialDiseases())
                .recommendedFoods(entity.getRecommendedFoods())
                .recommendedExercises(entity.getRecommendedExercises())
                .precautions(entity.getPrecautions())
                .analysisDate(entity.getAnalysisDate())
                .build();
    }
}
