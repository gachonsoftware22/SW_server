package com.sw.sw_ai_doc.domain.health.entity;

import com.sw.sw_ai_doc.domain.health.dto.HealthRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Health {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_id")
    private Long healthId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String symptom;

    @Column(columnDefinition = "TEXT")
    private String history;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateFrom(HealthRequestDto dto) {
        this.symptom = dto.getSymptom();
        this.history = dto.getHistory();
        this.note = dto.getNote();
        this.recordDate = dto.getRecordDate();
    }
}
