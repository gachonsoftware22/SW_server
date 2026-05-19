package com.sw.sw_ai_doc.domain.prescription.controller;

import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionRequestDto;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionResponseDto;
import com.sw.sw_ai_doc.domain.prescription.service.PrescriptionService;
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
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> createPrescription(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PrescriptionRequestDto request) {
        PrescriptionResponseDto data = prescriptionService.createPrescription(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "처방전이 생성되었습니다.", data));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getPrescriptionList(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PrescriptionResponseDto> data = prescriptionService.getPrescriptionList(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "처방전 목록 조회 성공", data));
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescriptionDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long prescriptionId) {
        PrescriptionResponseDto data = prescriptionService.getPrescriptionDetail(prescriptionId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "처방전 상세 조회 성공", data));
    }

    @PutMapping("/update/{prescriptionId}")
    public ResponseEntity<ApiResponse<Void>> updatePrescription(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionRequestDto request) {
        prescriptionService.updatePrescription(prescriptionId, principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "처방전이 수정되었습니다."));
    }

    @DeleteMapping("/delete/{prescriptionId}")
    public ResponseEntity<ApiResponse<Void>> deletePrescription(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "처방전이 삭제되었습니다."));
    }
}
