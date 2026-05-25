package com.sw.sw_ai_doc.domain.ai.controller;

import com.sw.sw_ai_doc.domain.ai.dto.AiResultResponseDto;
import com.sw.sw_ai_doc.domain.ai.service.AiDomainService;
import com.sw.sw_ai_doc.global.response.ApiResponse;
import com.sw.sw_ai_doc.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiDomainService aiDomainService;

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<AiResultResponseDto>> trigger(
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("[AI Trigger] 요청 수신 - userId={}", principal.userId());
        AiResultResponseDto data = aiDomainService.triggerAnalysis(principal.userId());
        log.info("[AI Trigger] 분석 완료 - userId={}, healthStatus={}", principal.userId(), data.getHealthStatus());
        return ResponseEntity.ok(ApiResponse.success(200, "AI 분석이 완료되었습니다.", data));
    }

    @GetMapping("/result/me")
    public ResponseEntity<ApiResponse<AiResultResponseDto>> getLatestResult(
            @AuthenticationPrincipal UserPrincipal principal) {
        Optional<AiResultResponseDto> result = aiDomainService.getLatestResult(principal.userId());
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ApiResponse.success(200, "AI 분석 결과 조회 성공", result.get()));
    }

    @GetMapping("/result/me/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        Optional<AiResultResponseDto> result = aiDomainService.getLatestResult(principal.userId());
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        AiResultResponseDto r = result.get();
        Map<String, Object> data = new HashMap<>();
        data.put("healthStatus", r.getHealthStatus());
        data.put("summaryNote", r.getSummaryNote());
        data.put("analysisDate", r.getAnalysisDate());
        return ResponseEntity.ok(ApiResponse.success(200, "요약 조회 성공", data));
    }

    @GetMapping("/result/me/diseases")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getDiseases(
            @AuthenticationPrincipal UserPrincipal principal) {
        return aiDomainService.getLatestResult(principal.userId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(200, "추정 질환 조회 성공",
                        Map.of("potentialDiseases", r.getPotentialDiseases()))))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/result/me/foods")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getFoods(
            @AuthenticationPrincipal UserPrincipal principal) {
        return aiDomainService.getLatestResult(principal.userId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(200, "권장 식품 조회 성공",
                        Map.of("recommendedFoods", r.getRecommendedFoods()))))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/result/me/exercises")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getExercises(
            @AuthenticationPrincipal UserPrincipal principal) {
        return aiDomainService.getLatestResult(principal.userId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(200, "권장 운동 조회 성공",
                        Map.of("recommendedExercises", r.getRecommendedExercises()))))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/result/me/precautions")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPrecautions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return aiDomainService.getLatestResult(principal.userId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(200, "주의사항 조회 성공",
                        Map.of("precautions", r.getPrecautions()))))
                .orElse(ResponseEntity.noContent().build());
    }
}
