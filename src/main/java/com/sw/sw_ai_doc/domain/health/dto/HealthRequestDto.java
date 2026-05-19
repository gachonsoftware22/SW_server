package com.sw.sw_ai_doc.domain.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class HealthRequestDto {
    @NotBlank(message = "증상을 입력해주세요.")
    private String symptom;

    private String history;
    private String note;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate recordDate;
}
