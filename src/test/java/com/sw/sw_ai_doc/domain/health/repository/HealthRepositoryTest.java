package com.sw.sw_ai_doc.domain.health.repository;

import com.sw.sw_ai_doc.domain.health.entity.Health;
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
class HealthRepositoryTest {

    @Autowired
    private HealthRepository healthRepository;

    @Autowired
    private EntityManager em;

    private Health buildHealth(Long userId, LocalDate recordDate) {
        return Health.builder()
                .userId(userId)
                .symptom("두통")
                .history("고혈압")
                .note("메모")
                .recordDate(recordDate)
                .build();
    }

    @Test
    void findByUserIdOrderByRecordDateDesc_orderedCorrectly() {
        em.persist(buildHealth(1L, LocalDate.of(2024, 1, 10)));
        em.persist(buildHealth(1L, LocalDate.of(2024, 3, 5)));
        em.persist(buildHealth(1L, LocalDate.of(2024, 2, 20)));
        em.flush();

        List<Health> result = healthRepository.findByUserIdOrderByRecordDateDesc(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRecordDate()).isEqualTo(LocalDate.of(2024, 3, 5));
        assertThat(result.get(1).getRecordDate()).isEqualTo(LocalDate.of(2024, 2, 20));
        assertThat(result.get(2).getRecordDate()).isEqualTo(LocalDate.of(2024, 1, 10));
    }

    @Test
    void findByUserIdOrderByRecordDateDesc_excludesOtherUsers() {
        em.persist(buildHealth(1L, LocalDate.now()));
        em.persist(buildHealth(2L, LocalDate.now()));
        em.flush();

        List<Health> result = healthRepository.findByUserIdOrderByRecordDateDesc(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void findByHealthIdAndUserId_found() {
        Health saved = em.merge(buildHealth(1L, LocalDate.now()));
        em.flush();

        Optional<Health> result = healthRepository.findByHealthIdAndUserId(saved.getHealthId(), 1L);

        assertThat(result).isPresent();
    }

    @Test
    void findByHealthIdAndUserId_wrongUser() {
        Health saved = em.merge(buildHealth(1L, LocalDate.now()));
        em.flush();

        Optional<Health> result = healthRepository.findByHealthIdAndUserId(saved.getHealthId(), 99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByHealthIdAndUserId_notFound() {
        Optional<Health> result = healthRepository.findByHealthIdAndUserId(999L, 1L);

        assertThat(result).isEmpty();
    }
}
