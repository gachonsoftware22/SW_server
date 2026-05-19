package com.sw.sw_ai_doc.domain.prescription.repository;

import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, PrescriptionStatus status);
    Optional<Prescription> findByPrescriptionIdAndUserId(Long prescriptionId, Long userId);
}
