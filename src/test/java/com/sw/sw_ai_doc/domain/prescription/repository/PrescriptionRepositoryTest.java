package com.sw.sw_ai_doc.domain.prescription.repository;

import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PrescriptionRepositoryTest {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private EntityManager em;

    private Prescription buildPrescription(Long userId, PrescriptionStatus status) {
        return Prescription.builder()
                .userId(userId)
                .prescriptionDate(LocalDate.now())
                .hospitalName("서울병원")
                .status(status)
                .build();
    }

    @Test
    void findByUserIdAndStatus_activeOnly() {
        em.persist(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.persist(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.persist(buildPrescription(1L, PrescriptionStatus.DELETED));
        em.flush();

        List<Prescription> result = prescriptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(1L, PrescriptionStatus.ACTIVE);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getStatus() == PrescriptionStatus.ACTIVE);
    }

    @Test
    void findByUserIdAndStatus_excludesOtherUsers() {
        em.persist(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.persist(buildPrescription(2L, PrescriptionStatus.ACTIVE));
        em.flush();

        List<Prescription> result = prescriptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(1L, PrescriptionStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void findByPrescriptionIdAndUserId_found() {
        Prescription saved = em.merge(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.flush();

        Optional<Prescription> result = prescriptionRepository
                .findByPrescriptionIdAndUserId(saved.getPrescriptionId(), 1L);

        assertThat(result).isPresent();
    }

    @Test
    void findByPrescriptionIdAndUserId_wrongUser() {
        Prescription saved = em.merge(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.flush();

        Optional<Prescription> result = prescriptionRepository
                .findByPrescriptionIdAndUserId(saved.getPrescriptionId(), 99L);

        assertThat(result).isEmpty();
    }

    @Test
    void softDelete_changesStatusToDeleted() {
        Prescription saved = em.merge(buildPrescription(1L, PrescriptionStatus.ACTIVE));
        em.flush();

        saved.softDelete();
        em.flush();
        em.clear();

        Prescription found = em.find(Prescription.class, saved.getPrescriptionId());
        assertThat(found.getStatus()).isEqualTo(PrescriptionStatus.DELETED);
    }
}
