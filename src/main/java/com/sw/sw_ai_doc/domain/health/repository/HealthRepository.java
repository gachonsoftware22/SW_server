package com.sw.sw_ai_doc.domain.health.repository;

import com.sw.sw_ai_doc.domain.health.entity.Health;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthRepository extends JpaRepository<Health, Long> {
    List<Health> findByUserIdOrderByRecordDateDesc(Long userId);
    Optional<Health> findByHealthIdAndUserId(Long healthId, Long userId);
}
