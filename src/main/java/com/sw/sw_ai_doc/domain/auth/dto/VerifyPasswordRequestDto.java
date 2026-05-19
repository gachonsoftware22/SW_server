package com.sw.sw_ai_doc.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VerifyPasswordRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
