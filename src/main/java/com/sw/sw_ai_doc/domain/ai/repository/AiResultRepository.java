package com.sw.sw_ai_doc.domain.ai.repository;

import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiResultRepository extends JpaRepository<AiResultEntity, Long> {
    Optional<AiResultEntity> findTopByUserIdOrderByAnalysisDateDesc(Long userId);
}
