package com.sw.sw_ai_doc.domain.ai.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.sw.sw_ai_doc.domain.ai.dto.AiResultRequestDto;
import com.sw.sw_ai_doc.domain.ai.entity.AiResultEntity;
import com.sw.sw_ai_doc.global.exception.AiAnalysisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiResultServiceImpl implements AiResultService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";

    private static final String SYSTEM_PROMPT = """
당신은 환자의 건강 데이터를 분석하는 의료 AI 어시스턴트입니다.
아래 지침을 엄격히 따르십시오.
1. 응답은 반드시 순수 JSON 객체 하나만 출력하십시오. 설명 문장, 마크다운 코드블록, 전후 공백을 절대 포함하지 마십시오.
2. 필드 이름과 타입은 아래 스키마를 정확히 따르십시오.
3. 배열 필드는 최대 5개 항목으로 제한하십시오.
4. health_status 값은 GOOD / CAUTION / WARNING 중 하나만 사용하십시오.
""";

    private static final String USER_PROMPT_TEMPLATE = """
다음은 환자의 최근 건강 기록 및 처방 데이터입니다.

[건강 기록 CSV]
%s

[처방 데이터 CSV]
%s

위 데이터를 분석하여 아래 JSON 스키마에 맞는 결과를 반환하십시오.

{
  "health_status": "GOOD | CAUTION | WARNING",
  "summary_note": "한국어 종합 건강 요약 (2~4문장)",
  "potential_diseases": ["질환명1", "질환명2"],
  "recommended_foods": ["식품1", "식품2"],
  "recommended_exercises": ["운동1", "운동2"],
  "precautions": "한국어 주의사항 (1~3문장)"
}
""";

    @Override
    public AiResultEntity analyze(AiResultRequestDto request) {
        log.debug("[AiResult] 분석 시작 - userId={}", request.getUserId());
        String userPrompt = USER_PROMPT_TEMPLATE.formatted(request.getHealthCsv(), request.getPrescriptionCsv());
        String rawResponse = callGeminiWithRetry(userPrompt);
        log.debug("[AiResult] Gemini 원본 응답:\n{}", rawResponse);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String healthStatus = root.path("health_status").asText();
            String summaryNote = root.path("summary_note").asText();
            List<String> diseases = objectMapper.convertValue(
                    root.path("potential_diseases"), new TypeReference<>() {});
            List<String> foods = objectMapper.convertValue(
                    root.path("recommended_foods"), new TypeReference<>() {});
            List<String> exercises = objectMapper.convertValue(
                    root.path("recommended_exercises"), new TypeReference<>() {});
            String precautions = root.path("precautions").asText();

            log.info("[AiResult] JSON 파싱 완료 - userId={}, healthStatus={}, diseases={}", request.getUserId(), healthStatus, diseases);

            return AiResultEntity.builder()
                    .userId(request.getUserId())
                    .healthStatus(healthStatus)
                    .summaryNote(summaryNote)
                    .potentialDiseases(diseases)
                    .recommendedFoods(foods)
                    .recommendedExercises(exercises)
                    .precautions(precautions)
                    .analysisDate(LocalDateTime.now())
                    .rawLlmResponse(rawResponse)
                    .build();
        } catch (Exception e) {
            log.error("[AiResult] JSON 파싱 실패 - userId={}, rawResponse={}", request.getUserId(), rawResponse, e);
            throw new AiAnalysisException("AI 응답 처리 중 오류가 발생했습니다.");
        }
    }

    private String callGeminiWithRetry(String userPrompt) {
        int maxRetries = 3;
        long delayMs = 1000;
        Exception lastException = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            log.info("[AiResult] Gemini 호출 시도 - attempt={}/{}", attempt + 1, maxRetries);
            try {
                String result = callGemini(userPrompt);
                log.info("[AiResult] Gemini 호출 성공 - attempt={}", attempt + 1);
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("[AiResult] Gemini 호출 실패 - attempt={}/{}, error={}", attempt + 1, maxRetries, e.getMessage());
                if (attempt < maxRetries - 1) {
                    log.info("[AiResult] {}ms 후 재시도...", delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    delayMs *= 2;
                }
            }
        }
        log.error("[AiResult] Gemini 호출 최종 실패 - 재시도 {}회 소진", maxRetries, lastException);
        throw new AiAnalysisException("AI 분석 서비스에 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    private String callGemini(String userPrompt) {
        Map<String, Object> requestBody = Map.of(
            "system_instruction", Map.of(
                "parts", List.of(Map.of("text", SYSTEM_PROMPT))
            ),
            "contents", List.of(
                Map.of("role", "user",
                       "parts", List.of(Map.of("text", userPrompt)))
            ),
            "generationConfig", Map.of("temperature", 0.3)
        );

        log.debug("[AiResult] Gemini API 요청 - model={}", model);
        RestClient restClient = RestClient.create();
        String responseBody = restClient.post()
                .uri(GEMINI_URL, model, apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        log.debug("[AiResult] Gemini HTTP 응답 수신 - bodyLength={}", responseBody != null ? responseBody.length() : 0);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates")
                       .get(0)
                       .path("content")
                       .path("parts")
                       .get(0)
                       .path("text")
                       .asText();
        } catch (Exception e) {
            log.error("[AiResult] Gemini 응답 구조 파싱 실패 - responseBody={}", responseBody, e);
            throw new AiAnalysisException("AI 응답 파싱 실패: " + responseBody);
        }
    }
}
