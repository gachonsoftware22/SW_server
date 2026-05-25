package com.sw.sw_ai_doc.domain.member.controller;

import com.sw.sw_ai_doc.domain.member.dto.request.MemberUpdateRequest;
import com.sw.sw_ai_doc.domain.member.dto.request.SignupRequest;
import com.sw.sw_ai_doc.domain.member.dto.response.MemberInfoResponse;
import com.sw.sw_ai_doc.domain.member.dto.response.SignupResponse;
import com.sw.sw_ai_doc.global.response.ApiResponse;
import com.sw.sw_ai_doc.global.security.UserPrincipal;
import com.sw.sw_ai_doc.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        SignupResponse data = memberService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(200, "회원가입 완료", data));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal) {
        MemberInfoResponse data = memberService.getMyInfo(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "조회 성공", data));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody MemberUpdateRequest request) {
        memberService.updateMyInfo(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "회원 정보가 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserPrincipal principal) {
        memberService.withdraw(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "회원 탈퇴가 완료되었습니다."));
    }

}
