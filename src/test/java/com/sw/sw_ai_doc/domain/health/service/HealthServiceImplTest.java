package com.sw.sw_ai_doc.domain.health.service;

import com.sw.sw_ai_doc.domain.health.dto.HealthRequestDto;
import com.sw.sw_ai_doc.domain.health.dto.HealthResponseDto;
import com.sw.sw_ai_doc.domain.health.entity.Health;
import com.sw.sw_ai_doc.domain.health.repository.HealthRepository;
import com.sw.sw_ai_doc.global.exception.HealthNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HealthServiceImplTest {

    @InjectMocks
    private HealthServiceImpl healthService;

    @Mock
    private HealthRepository healthRepository;

    private Health buildHealth(Long healthId, Long userId) {
        Health health = Health.builder()
                .userId(userId)
                .symptom("두통")
                .history("고혈압")
                .note("메모")
                .recordDate(LocalDate.of(2024, 5, 1))
                .build();
        ReflectionTestUtils.setField(health, "healthId", healthId);
        ReflectionTestUtils.setField(health, "createdAt", LocalDateTime.now());
        return health;
    }

    private HealthRequestDto buildRequest() {
        HealthRequestDto req = new HealthRequestDto();
        ReflectionTestUtils.setField(req, "symptom", "두통");
        ReflectionTestUtils.setField(req, "history", "고혈압");
        ReflectionTestUtils.setField(req, "note", "메모");
        ReflectionTestUtils.setField(req, "recordDate", LocalDate.of(2024, 5, 1));
        return req;
    }

    @Test
    void createHealth_success() {
        Health saved = buildHealth(1L, 1L);
        given(healthRepository.save(any(Health.class))).willReturn(saved);

        HealthResponseDto response = healthService.createHealth(1L, buildRequest());

        assertThat(response.getSymptom()).isEqualTo("두통");
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(healthRepository).save(any(Health.class));
    }

    @Test
    void getHealthList_returnsAll() {
        List<Health> healthList = List.of(
                buildHealth(2L, 1L),
                buildHealth(1L, 1L)
        );
        given(healthRepository.findByUserIdOrderByRecordDateDesc(1L)).willReturn(healthList);

        List<HealthResponseDto> result = healthService.getHealthList(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getHealthDetail_success() {
        Health health = buildHealth(1L, 1L);
        given(healthRepository.findByHealthIdAndUserId(1L, 1L)).willReturn(Optional.of(health));

        HealthResponseDto response = healthService.getHealthDetail(1L, 1L);

        assertThat(response.getHealthId()).isEqualTo(1L);
        assertThat(response.getSymptom()).isEqualTo("두통");
    }

    @Test
    void getHealthDetail_notFound_throwsException() {
        given(healthRepository.findByHealthIdAndUserId(999L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.getHealthDetail(999L, 1L))
                .isInstanceOf(HealthNotFoundException.class);
    }

    @Test
    void getHealthDetail_wrongUser_throwsException() {
        given(healthRepository.findByHealthIdAndUserId(1L, 99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.getHealthDetail(1L, 99L))
                .isInstanceOf(HealthNotFoundException.class);
    }

    @Test
    void updateHealth_success() {
        Health health = buildHealth(1L, 1L);
        given(healthRepository.findByHealthIdAndUserId(1L, 1L)).willReturn(Optional.of(health));

        HealthRequestDto updateReq = new HealthRequestDto();
        ReflectionTestUtils.setField(updateReq, "symptom", "복통");
        ReflectionTestUtils.setField(updateReq, "history", "");
        ReflectionTestUtils.setField(updateReq, "note", "새 메모");
        ReflectionTestUtils.setField(updateReq, "recordDate", LocalDate.of(2024, 6, 1));

        HealthResponseDto response = healthService.updateHealth(1L, 1L, updateReq);

        assertThat(response.getSymptom()).isEqualTo("복통");
        assertThat(response.getRecordDate()).isEqualTo(LocalDate.of(2024, 6, 1));
    }

    @Test
    void deleteHealth_success() {
        Health health = buildHealth(1L, 1L);
        given(healthRepository.findByHealthIdAndUserId(1L, 1L)).willReturn(Optional.of(health));

        healthService.deleteHealth(1L, 1L);

        verify(healthRepository).delete(health);
    }

    @Test
    void deleteHealth_notFound_throwsException() {
        given(healthRepository.findByHealthIdAndUserId(999L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.deleteHealth(999L, 1L))
                .isInstanceOf(HealthNotFoundException.class);
    }
}
