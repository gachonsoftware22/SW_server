package com.sw.sw_ai_doc.domain.ai.controller;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;
import com.sw.sw_ai_doc.domain.ai.service.AiDomainService;
import com.sw.sw_ai_doc.global.exception.AiAnalysisException;
import com.sw.sw_ai_doc.support.SecurityTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AiDomainService aiDomainService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private AiResultResponseDto buildResult() {
        return AiResultResponseDto.builder()
                .resultId(1L)
                .userId(1L)
                .healthStatus("GOOD")
                .summaryNote("건강 양호")
                .potentialDiseases(List.of("고혈압"))
                .recommendedFoods(List.of("사과", "당근"))
                .recommendedExercises(List.of("조깅", "수영"))
                .precautions("충분한 수면 필요")
                .analysisDate(LocalDateTime.now())
                .build();
    }

    @Test
    void triggerAnalysis_success() throws Exception {
        given(aiDomainService.triggerAnalysis(1L)).willReturn(buildResult());

        mockMvc.perform(post("/api/ai/trigger")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.healthStatus").value("GOOD"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void triggerAnalysis_aiFailure_returns500() throws Exception {
        given(aiDomainService.triggerAnalysis(1L)).willThrow(new AiAnalysisException("Gemini API 호출 실패"));

        mockMvc.perform(post("/api/ai/trigger")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getLatestResult_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.healthStatus").value("GOOD"));
    }

    @Test
    void getLatestResult_noData_returns204() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/ai/result/me")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSummary_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me/summary")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.healthStatus").value("GOOD"))
                .andExpect(jsonPath("$.data.summaryNote").value("건강 양호"));
    }

    @Test
    void getSummary_noData_returns204() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/ai/result/me/summary")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getDiseases_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me/diseases")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.potentialDiseases[0]").value("고혈압"));
    }

    @Test
    void getFoods_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me/foods")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendedFoods[0]").value("사과"));
    }

    @Test
    void getExercises_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me/exercises")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendedExercises[0]").value("조깅"));
    }

    @Test
    void getPrecautions_success() throws Exception {
        given(aiDomainService.getLatestResult(1L)).willReturn(Optional.of(buildResult()));

        mockMvc.perform(get("/api/ai/result/me/precautions")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.precautions").value("충분한 수면 필요"));
    }
}
