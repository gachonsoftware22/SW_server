package com.sw.sw_ai_doc.domain.prescription.service;

import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionRequestDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionResponseDto;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponseDto createPrescription(Long userId, PrescriptionRequestDto dto);
    List<PrescriptionResponseDto> getPrescriptionList(Long userId);
    PrescriptionResponseDto getPrescriptionDetail(Long prescriptionId, Long userId);
    void updatePrescription(Long prescriptionId, Long userId, PrescriptionRequestDto dto);
    void deletePrescription(Long prescriptionId, Long userId);
}
