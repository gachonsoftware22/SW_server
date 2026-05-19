package com.sw.sw_ai_doc.domain.member.dto.request;

import lombok.Getter;

@Getter
public class MemberUpdateRequest {
    private String name;
    private String phone;
    private String newPassword;
}
