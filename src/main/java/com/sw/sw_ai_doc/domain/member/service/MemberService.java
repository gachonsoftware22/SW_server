package com.sw.sw_ai_doc.domain.member.service;

import com.sw.sw_ai_doc.domain.member.dto.request.*;
import com.sw.sw_ai_doc.domain.member.dto.response.MemberInfoResponse;
import com.sw.sw_ai_doc.domain.member.dto.response.SignupResponse;

public interface MemberService {
    SignupResponse signup(SignupRequest request);
    MemberInfoResponse getMyInfo(Long userId);
    void updateMyInfo(Long userId, MemberUpdateRequest request);
    void withdraw(Long userId, WithdrawRequest request);
}
