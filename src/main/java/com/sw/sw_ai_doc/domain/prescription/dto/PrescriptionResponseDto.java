package com.sw.sw_ai_doc.domain.prescription.dto;

import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionDetail;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PrescriptionResponseDto {
    private Long prescriptionId;
    private LocalDate prescriptionDate;
    private String hospitalName;
    private List<MedicineResponseDto> medicineList;

    @Getter
    @Builder
    public static class MedicineResponseDto {
        private String medicineName;
        private String dosage;
        private String duration;

        public static MedicineResponseDto from(PrescriptionDetail detail) {
            return MedicineResponseDto.builder()
                    .medicineName(detail.getMedicineName())
                    .dosage(detail.getDosage())
                    .duration(detail.getDuration())
                    .build();
        }
    }

    public static PrescriptionResponseDto from(Prescription prescription) {
        List<MedicineResponseDto> medicines = prescription.getDetails().stream()
                .map(MedicineResponseDto::from)
                .collect(Collectors.toList());
        return PrescriptionResponseDto.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .prescriptionDate(prescription.getPrescriptionDate())
                .hospitalName(prescription.getHospitalName())
                .medicineList(medicines)
                .build();
    }

    public static PrescriptionResponseDto fromSummary(Prescription prescription) {
        return PrescriptionResponseDto.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .prescriptionDate(prescription.getPrescriptionDate())
                .hospitalName(prescription.getHospitalName())
                .build();
    }
}
