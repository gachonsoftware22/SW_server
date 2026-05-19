package com.sw.sw_ai_doc.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;
}
