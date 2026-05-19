package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.health.entity.Health;
import com.sw.sw_ai_doc.domain.health.repository.HealthRepository;
import com.sw.sw_ai_doc.domain.prescription.entity.Prescription;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionDetail;
import com.sw.sw_ai_doc.domain.prescription.entity.PrescriptionStatus;
import com.sw.sw_ai_doc.domain.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiDatasetService {

    private final HealthRepository healthRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AiResultRequestDto buildDataset(Long userId) {
        String healthCsv = buildHealthCsv(userId);
        String prescriptionCsv = buildPrescriptionCsv(userId);
        return new AiResultRequestDto(userId, healthCsv, prescriptionCsv);
    }

    private String buildHealthCsv(Long userId) {
        List<Health> healthList = healthRepository.findByUserIdOrderByRecordDateDesc(userId);
        if (healthList.isEmpty()) {
            return "[health]\nhealth_id,symptom,history,note,record_date\n(데이터 없음)";
        }
        StringBuilder sb = new StringBuilder("[health]\nhealth_id,symptom,history,note,record_date\n");
        for (Health h : healthList) {
            sb.append(h.getHealthId()).append(",")
              .append(quote(h.getSymptom())).append(",")
              .append(quote(h.getHistory())).append(",")
              .append(quote(h.getNote())).append(",")
              .append(quote(h.getRecordDate() != null ? h.getRecordDate().toString() : ""))
              .append("\n");
        }
        return sb.toString();
    }

    private String buildPrescriptionCsv(Long userId) {
        List<Prescription> prescriptions = prescriptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, PrescriptionStatus.ACTIVE);
        if (prescriptions.isEmpty()) {
            return "[prescription]\nprescription_id,prescription_date,hospital_name\n(데이터 없음)\n\n[prescription_detail]\ndetail_id,prescription_id,medicine_name,dosage,duration\n(데이터 없음)";
        }

        StringBuilder prescSb = new StringBuilder("[prescription]\nprescription_id,prescription_date,hospital_name\n");
        StringBuilder detailSb = new StringBuilder("[prescription_detail]\ndetail_id,prescription_id,medicine_name,dosage,duration\n");

        for (Prescription p : prescriptions) {
            prescSb.append(p.getPrescriptionId()).append(",")
                   .append(quote(p.getPrescriptionDate() != null ? p.getPrescriptionDate().toString() : "")).append(",")
                   .append(quote(p.getHospitalName()))
                   .append("\n");
            for (PrescriptionDetail d : p.getDetails()) {
                detailSb.append(d.getDetailId()).append(",")
                        .append(p.getPrescriptionId()).append(",")
                        .append(quote(d.getMedicineName())).append(",")
                        .append(quote(d.getDosage())).append(",")
                        .append(quote(d.getDuration()))
                        .append("\n");
            }
        }
        return prescSb + "\n" + detailSb;
    }

    private String quote(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
