package com.sw.sw_ai_doc.domain.prescription.service;

import com.sw.sw_ai_doc.domain.prescription.dto.MedicineDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionRequestDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionResponseDto;
import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionStatus;
import com.sw.sw_ai_doc.domain.prescription.repository.PrescriptionRepository;
import com.sw.sw_ai_doc.global.exception.PrescriptionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    private Prescription buildPrescription(Long id, Long userId, PrescriptionStatus status) {
        Prescription p = Prescription.builder()
                .userId(userId)
                .prescriptionDate(LocalDate.of(2024, 3, 1))
                .hospitalName("서울병원")
                .status(status)
                .build();
        ReflectionTestUtils.setField(p, "prescriptionId", id);
        return p;
    }

    private PrescriptionRequestDto buildRequest() {
        PrescriptionRequestDto req = new PrescriptionRequestDto();
        ReflectionTestUtils.setField(req, "prescriptionDate", LocalDate.of(2024, 3, 1));
        ReflectionTestUtils.setField(req, "hospitalName", "서울병원");

        MedicineDto medicine = new MedicineDto();
        ReflectionTestUtils.setField(medicine, "medicineName", "아스피린");
        ReflectionTestUtils.setField(medicine, "dosage", "1정");
        ReflectionTestUtils.setField(medicine, "duration", "7일");

        ReflectionTestUtils.setField(req, "medicineList", List.of(medicine));
        return req;
    }

    @Test
    void createPrescription_success() {
        Prescription saved = buildPrescription(1L, 1L, PrescriptionStatus.ACTIVE);
        given(prescriptionRepository.save(any(Prescription.class))).willReturn(saved);

        PrescriptionResponseDto response = prescriptionService.createPrescription(1L, buildRequest());

        assertThat(response.getHospitalName()).isEqualTo("서울병원");
        assertThat(response.getPrescriptionId()).isEqualTo(1L);
    }

    @Test
    void getPrescriptionList_returnsActiveOnly() {
        List<Prescription> activeList = List.of(
                buildPrescription(1L, 1L, PrescriptionStatus.ACTIVE),
                buildPrescription(2L, 1L, PrescriptionStatus.ACTIVE)
        );
        given(prescriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, PrescriptionStatus.ACTIVE))
                .willReturn(activeList);

        List<PrescriptionResponseDto> result = prescriptionService.getPrescriptionList(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getPrescriptionDetail_success() {
        Prescription p = buildPrescription(1L, 1L, PrescriptionStatus.ACTIVE);
        given(prescriptionRepository.findByPrescriptionIdAndUserId(1L, 1L)).willReturn(Optional.of(p));

        PrescriptionResponseDto response = prescriptionService.getPrescriptionDetail(1L, 1L);

        assertThat(response.getPrescriptionId()).isEqualTo(1L);
    }

    @Test
    void getPrescriptionDetail_deletedStatus_throwsException() {
        Prescription deleted = buildPrescription(1L, 1L, PrescriptionStatus.DELETED);
        given(prescriptionRepository.findByPrescriptionIdAndUserId(1L, 1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> prescriptionService.getPrescriptionDetail(1L, 1L))
                .isInstanceOf(PrescriptionNotFoundException.class);
    }

    @Test
    void getPrescriptionDetail_notFound_throwsException() {
        given(prescriptionRepository.findByPrescriptionIdAndUserId(999L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.getPrescriptionDetail(999L, 1L))
                .isInstanceOf(PrescriptionNotFoundException.class);
    }

    @Test
    void deletePrescription_softDelete() {
        Prescription p = buildPrescription(1L, 1L, PrescriptionStatus.ACTIVE);
        given(prescriptionRepository.findByPrescriptionIdAndUserId(1L, 1L)).willReturn(Optional.of(p));

        prescriptionService.deletePrescription(1L, 1L);

        assertThat(p.getStatus()).isEqualTo(PrescriptionStatus.DELETED);
    }

    @Test
    void deletePrescription_alreadyDeleted_throwsException() {
        Prescription deleted = buildPrescription(1L, 1L, PrescriptionStatus.DELETED);
        given(prescriptionRepository.findByPrescriptionIdAndUserId(1L, 1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> prescriptionService.deletePrescription(1L, 1L))
                .isInstanceOf(PrescriptionNotFoundException.class);
    }

    @Test
    void updatePrescription_success() {
        Prescription p = buildPrescription(1L, 1L, PrescriptionStatus.ACTIVE);
        ReflectionTestUtils.setField(p, "details", new ArrayList<>());
        given(prescriptionRepository.findByPrescriptionIdAndUserId(1L, 1L)).willReturn(Optional.of(p));

        PrescriptionRequestDto updateReq = buildRequest();
        ReflectionTestUtils.setField(updateReq, "hospitalName", "부산병원");

        prescriptionService.updatePrescription(1L, 1L, updateReq);

        assertThat(p.getHospitalName()).isEqualTo("부산병원");
    }
}
