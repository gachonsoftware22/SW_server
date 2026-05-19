package com.sw.sw_ai_doc.domain.prescription.service;

import com.sw.sw_ai_doc.domain.prescription.dto.MedicineDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionRequestDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionResponseDto;
import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionDetail;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionStatus;
import com.sw.sw_ai_doc.domain.prescription.repository.PrescriptionRepository;
import com.sw.sw_ai_doc.global.exception.PrescriptionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    public PrescriptionResponseDto createPrescription(Long userId, PrescriptionRequestDto dto) {
        Prescription prescription = Prescription.builder()
                .userId(userId)
                .prescriptionDate(dto.getPrescriptionDate())
                .hospitalName(dto.getHospitalName())
                .status(PrescriptionStatus.ACTIVE)
                .build();

        dto.getMedicineList().forEach(m -> {
            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(prescription)
                    .medicineName(m.getMedicineName())
                    .dosage(m.getDosage())
                    .duration(m.getDuration())
                    .build();
            prescription.getDetails().add(detail);
        });

        return PrescriptionResponseDto.from(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionList(Long userId) {
        return prescriptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, PrescriptionStatus.ACTIVE)
                .stream()
                .map(PrescriptionResponseDto::fromSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionDetail(Long prescriptionId, Long userId) {
        Prescription prescription = findByIdAndUser(prescriptionId, userId);
        if (prescription.getStatus() == PrescriptionStatus.DELETED) {
            throw new PrescriptionNotFoundException();
        }
        return PrescriptionResponseDto.from(prescription);
    }

    @Override
    public void updatePrescription(Long prescriptionId, Long userId, PrescriptionRequestDto dto) {
        Prescription prescription = findActiveByIdAndUser(prescriptionId, userId);
        prescription.update(dto.getPrescriptionDate(), dto.getHospitalName());

        prescription.getDetails().clear();
        dto.getMedicineList().forEach(m -> {
            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(prescription)
                    .medicineName(m.getMedicineName())
                    .dosage(m.getDosage())
                    .duration(m.getDuration())
                    .build();
            prescription.getDetails().add(detail);
        });
    }

    @Override
    public void deletePrescription(Long prescriptionId, Long userId) {
        Prescription prescription = findActiveByIdAndUser(prescriptionId, userId);
        prescription.softDelete();
    }

    private Prescription findByIdAndUser(Long prescriptionId, Long userId) {
        return prescriptionRepository.findByPrescriptionIdAndUserId(prescriptionId, userId)
                .orElseThrow(PrescriptionNotFoundException::new);
    }

    private Prescription findActiveByIdAndUser(Long prescriptionId, Long userId) {
        Prescription prescription = findByIdAndUser(prescriptionId, userId);
        if (prescription.getStatus() == PrescriptionStatus.DELETED) {
            throw new PrescriptionNotFoundException();
        }
        return prescription;
    }
}
