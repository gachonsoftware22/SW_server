package com.sw.sw_ai_doc.domain.health.service;

import com.sw.sw_ai_doc.domain.health.dto.HealthRequestDto;
import com.sw.sw_ai_doc.domain.health.dto.HealthResponseDto;
import com.sw.sw_ai_doc.domain.health.entity.Health;
import com.sw.sw_ai_doc.domain.health.repository.HealthRepository;
import com.sw.sw_ai_doc.global.exception.HealthNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthServiceImpl implements HealthService {

    private final HealthRepository healthRepository;

    @Override
    public HealthResponseDto createHealth(Long userId, HealthRequestDto dto) {
        Health health = Health.builder()
                .userId(userId)
                .symptom(dto.getSymptom())
                .history(dto.getHistory())
                .note(dto.getNote())
                .recordDate(dto.getRecordDate())
                .build();
        return HealthResponseDto.from(healthRepository.save(health));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthResponseDto> getHealthList(Long userId) {
        return healthRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
                .map(HealthResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthResponseDto getHealthDetail(Long healthId, Long userId) {
        Health health = findByIdAndUser(healthId, userId);
        return HealthResponseDto.from(health);
    }

    @Override
    public HealthResponseDto updateHealth(Long healthId, Long userId, HealthRequestDto dto) {
        Health health = findByIdAndUser(healthId, userId);
        health.updateFrom(dto);
        return HealthResponseDto.from(health);
    }

    @Override
    public void deleteHealth(Long healthId, Long userId) {
        Health health = findByIdAndUser(healthId, userId);
        healthRepository.delete(health);
    }

    private Health findByIdAndUser(Long healthId, Long userId) {
        return healthRepository.findByHealthIdAndUserId(healthId, userId)
                .orElseThrow(HealthNotFoundException::new);
    }
}
