package com.sw.sw_ai_doc.domain.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class PrescriptionRequestDto {
    @NotNull(message = "처방일을 입력해주세요.")
    private LocalDate prescriptionDate;

    @NotBlank(message = "병원명을 입력해주세요.")
    private String hospitalName;

    @NotNull(message = "처방약 정보를 입력해주세요.")
    @Valid
    private List<MedicineDto> medicineList;
}
