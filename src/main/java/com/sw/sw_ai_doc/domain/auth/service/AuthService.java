package com.sw.sw_ai_doc.domain.auth.service;

import com.sw.sw_ai_doc.domain.auth.dto.LoginRequestDto;
import com.sw.sw_ai_doc.domain.auth.dto.LoginResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.TokenRefreshResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.VerifyPasswordRequestDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request, HttpServletResponse response);
    void logout(Long userId, String refreshToken);
    TokenRefreshResponseDto refresh(String refreshToken, HttpServletResponse response);
    void verifyPassword(Long userId, VerifyPasswordRequestDto request);
}
