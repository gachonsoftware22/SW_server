package com.sw.sw_ai_doc.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long userId;
    private String loginId;
    private String message;
}
