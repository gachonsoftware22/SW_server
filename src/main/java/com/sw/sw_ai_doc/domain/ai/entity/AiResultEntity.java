package com.sw.sw_ai_doc.domain.ai.entity;

import com.sw.sw_ai_doc.global.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AiResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "health_status", length = 20, nullable = false)
    private String healthStatus;

    @Column(name = "summary_note", columnDefinition = "TEXT", nullable = false)
    private String summaryNote;

    @Convert(converter = StringListConverter.class)
    @Column(name = "potential_diseases", columnDefinition = "json")
    private List<String> potentialDiseases;

    @Convert(converter = StringListConverter.class)
    @Column(name = "recommended_foods", columnDefinition = "json")
    private List<String> recommendedFoods;

    @Convert(converter = StringListConverter.class)
    @Column(name = "recommended_exercises", columnDefinition = "json")
    private List<String> recommendedExercises;

    @Column(columnDefinition = "TEXT")
    private String precautions;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    @Column(name = "raw_llm_response", columnDefinition = "json")
    private String rawLlmResponse;
}
