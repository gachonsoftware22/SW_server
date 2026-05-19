package com.sw.sw_ai_doc.domain.health.controller;

import com.sw.sw_ai_doc.domain.health.dto.HealthRequestDto;
import com.sw.sw_ai_doc.domain.health.dto.HealthResponseDto;
import com.sw.sw_ai_doc.domain.health.service.HealthService;
import com.sw.sw_ai_doc.global.response.ApiResponse;
import com.sw.sw_ai_doc.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<HealthResponseDto>> createHealth(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody HealthRequestDto request) {
        HealthResponseDto data = healthService.createHealth(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "건강 기록이 생성되었습니다.", data));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<HealthResponseDto>>> getHealthList(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<HealthResponseDto> data = healthService.getHealthList(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "건강 기록 목록 조회 성공", data));
    }

    @GetMapping("/{healthId}")
    public ResponseEntity<ApiResponse<HealthResponseDto>> getHealthDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long healthId) {
        HealthResponseDto data = healthService.getHealthDetail(healthId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "건강 기록 조회 성공", data));
    }

    @PutMapping("/update/{healthId}")
    public ResponseEntity<ApiResponse<HealthResponseDto>> updateHealth(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long healthId,
            @Valid @RequestBody HealthRequestDto request) {
        HealthResponseDto data = healthService.updateHealth(healthId, principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "건강 기록이 수정되었습니다.", data));
    }

    @DeleteMapping("/delete/{healthId}")
    public ResponseEntity<ApiResponse<Void>> deleteHealth(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long healthId) {
        healthService.deleteHealth(healthId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "건강 기록이 삭제되었습니다."));
    }
}
