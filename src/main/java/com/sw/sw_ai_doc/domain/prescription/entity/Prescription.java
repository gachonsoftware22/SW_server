package com.sw.sw_ai_doc.domain.prescription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;

    @Column(name = "hospital_name", length = 100, nullable = false)
    private String hospitalName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    @Builder.Default
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionDetail> details = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(LocalDate prescriptionDate, String hospitalName) {
        this.prescriptionDate = prescriptionDate;
        this.hospitalName = hospitalName;
    }

    public void softDelete() {
        this.status = PrescriptionStatus.DELETED;
    }
}
