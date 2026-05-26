package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;
import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import com.sw.sw_ai_doc.domain.ai.repository.AiResultRepository;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDomainServiceImpl implements AiDomainService {

    private final AiDatasetService aiDatasetService;
    private final AiResultService aiResultService;
    private final AiResultRepository aiResultRepository;
    private final UserRepository userRepository;

    @Override
    public AiResultResponseDto triggerAnalysis(Long userId) {
        log.debug("[AiDomain] 사용자 검증 - userId={}", userId);
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        log.debug("[AiDomain] 데이터셋 빌드 시작 - userId={}", userId);
        AiResultRequestDto dataset = aiDatasetService.buildDataset(userId);

        log.info("[AiDomain] Gemini 분석 요청 - userId={}", userId);
        AiResultEntity result = aiResultService.analyze(dataset);

        AiResultEntity saved = aiResultRepository.save(result);
        log.info("[AiDomain] 분석 결과 저장 완료 - userId={}, resultId={}", userId, saved.getResultId());
        return AiResultResponseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiResultResponseDto> getLatestResult(Long userId) {
        return aiResultRepository.findTopByUserIdOrderByAnalysisDateDesc(userId)
                .map(AiResultResponseDto::from);
    }
}
