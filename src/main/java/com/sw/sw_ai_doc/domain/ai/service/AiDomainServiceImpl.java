package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;
import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import com.sw.sw_ai_doc.domain.ai.repository.AiResultRepository;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiDomainServiceImpl implements AiDomainService {

    private final AiDatasetService aiDatasetService;
    private final AiResultService aiResultService;
    private final AiResultRepository aiResultRepository;
    private final UserRepository userRepository;

    @Override
    public AiResultResponseDto triggerAnalysis(Long userId) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        AiResultRequestDto dataset = aiDatasetService.buildDataset(userId);
        AiResultEntity result = aiResultService.analyze(dataset);
        AiResultEntity saved = aiResultRepository.save(result);
        return AiResultResponseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiResultResponseDto> getLatestResult(Long userId) {
        return aiResultRepository.findTopByUserIdOrderByAnalysisDateDesc(userId)
                .map(AiResultResponseDto::from);
    }
}
