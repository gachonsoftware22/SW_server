package com.sw.sw_ai_doc.domain.ai.service;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;
import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import com.sw.sw_ai_doc.domain.ai.repository.AiResultRepository;
import com.sw.sw_ai_doc.domain.member.entity.User;
import com.sw.sw_ai_doc.domain.member.entity.UserStatus;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.UserNotFoundException;
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
class AiDomainServiceImplTest {

    @InjectMocks
    private AiDomainServiceImpl aiDomainService;

    @Mock
    private AiDatasetService aiDatasetService;

    @Mock
    private AiResultService aiResultService;

    @Mock
    private AiResultRepository aiResultRepository;

    @Mock
    private UserRepository userRepository;

    private User buildUser(Long id) {
        User user = User.builder()
                .loginId("testuser")
                .password("encoded")
                .name("홍길동")
                .email("test@test.com")
                .phone("01012345678")
                .birthDate(LocalDate.of(1995, 1, 1))
                .gender("M")
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private AiResultEntity buildResultEntity(Long resultId, Long userId) {
        return AiResultEntity.builder()
                .userId(userId)
                .healthStatus("GOOD")
                .summaryNote("건강 양호")
                .potentialDiseases(List.of("고혈압"))
                .recommendedFoods(List.of("사과"))
                .recommendedExercises(List.of("조깅"))
                .precautions("충분한 수면 필요")
                .analysisDate(LocalDateTime.now())
                .build();
    }

    @Test
    void triggerAnalysis_success() {
        User user = buildUser(1L);
        AiResultRequestDto dataset = new AiResultRequestDto(
                1L, "date,symptom\n2024-01-01,두통", "date,hospital\n2024-01-01,서울병원");
        AiResultEntity resultEntity = buildResultEntity(1L, 1L);
        AiResultEntity savedEntity = buildResultEntity(1L, 1L);
        ReflectionTestUtils.setField(savedEntity, "resultId", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(aiDatasetService.buildDataset(1L)).willReturn(dataset);
        given(aiResultService.analyze(dataset)).willReturn(resultEntity);
        given(aiResultRepository.save(resultEntity)).willReturn(savedEntity);

        AiResultResponseDto response = aiDomainService.triggerAnalysis(1L);

        assertThat(response.getHealthStatus()).isEqualTo("GOOD");
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(aiDatasetService).buildDataset(1L);
        verify(aiResultService).analyze(dataset);
        verify(aiResultRepository).save(resultEntity);
    }

    @Test
    void triggerAnalysis_userNotFound_throwsException() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> aiDomainService.triggerAnalysis(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getLatestResult_hasResult() {
        AiResultEntity entity = buildResultEntity(1L, 1L);
        ReflectionTestUtils.setField(entity, "resultId", 1L);
        given(aiResultRepository.findTopByUserIdOrderByAnalysisDateDesc(1L)).willReturn(Optional.of(entity));

        Optional<AiResultResponseDto> result = aiDomainService.getLatestResult(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getHealthStatus()).isEqualTo("GOOD");
    }

    @Test
    void getLatestResult_noResult_returnsEmpty() {
        given(aiResultRepository.findTopByUserIdOrderByAnalysisDateDesc(1L)).willReturn(Optional.empty());

        Optional<AiResultResponseDto> result = aiDomainService.getLatestResult(1L);

        assertThat(result).isEmpty();
    }
}
