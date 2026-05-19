package com.sw.sw_ai_doc.domain.member.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignupRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$",
             message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String phone;

    @NotNull(message = "생년월일을 입력해주세요.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    private String gender;
}
