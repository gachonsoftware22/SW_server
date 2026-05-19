package com.sw.sw_ai_doc.domain.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MedicineDto {
    @NotBlank(message = "약품명을 입력해주세요.")
    private String medicineName;

    @NotBlank(message = "용법을 입력해주세요.")
    private String dosage;

    @NotBlank(message = "복용 기간을 입력해주세요.")
    private String duration;
}
