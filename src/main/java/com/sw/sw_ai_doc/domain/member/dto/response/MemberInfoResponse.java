package com.sw.sw_ai_doc.domain.member.dto.response;

import com.sw.sw_ai_doc.domain.member.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MemberInfoResponse {
    private Long userId;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberInfoResponse from(User user) {
        return MemberInfoResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
