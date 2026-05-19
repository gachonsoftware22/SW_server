package com.sw.sw_ai_doc.domain.auth.controller;

import com.sw.sw_ai_doc.domain.auth.dto.LoginRequestDto;
import com.sw.sw_ai_doc.domain.auth.dto.LoginResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.TokenRefreshResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.VerifyPasswordRequestDto;
import com.sw.sw_ai_doc.domain.auth.service.AuthService;
import com.sw.sw_ai_doc.global.response.ApiResponse;
import com.sw.sw_ai_doc.global.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        LoginResponseDto data = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(200, "로그인 성공", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);
        authService.logout(principal.userId(), refreshToken);
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.success(200, "로그아웃 되었습니다."));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);
        TokenRefreshResponseDto data = authService.refresh(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(200, "토큰 재발급 성공", data));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyPasswordRequestDto request) {
        authService.verifyPassword(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "본인 확인 성공"));
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
