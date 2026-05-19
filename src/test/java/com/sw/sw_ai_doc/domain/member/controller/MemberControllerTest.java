package com.sw.sw_ai_doc.domain.member.controller;

import tools.jackson.databind.ObjectMapper;
import com.sw.sw_ai_doc.domain.member.dto.response.MemberInfoResponse;
import com.sw.sw_ai_doc.domain.member.dto.response.SignupResponse;
import com.sw.sw_ai_doc.domain.member.service.MemberService;
import com.sw.sw_ai_doc.global.exception.DuplicateEmailException;
import com.sw.sw_ai_doc.global.exception.DuplicateLoginIdException;
import com.sw.sw_ai_doc.support.SecurityTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MemberControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Map<String, Object> validSignupBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("loginId", "newuser");
        body.put("password", "Password1!");
        body.put("name", "홍길동");
        body.put("email", "new@test.com");
        body.put("phone", "01012345678");
        body.put("birthDate", "1995-01-01");
        body.put("gender", "M");
        return body;
    }

    @Test
    void signup_success() throws Exception {
        SignupResponse response = new SignupResponse(1L, "newuser", "가입이 정상적으로 처리되었습니다.");
        given(memberService.signup(any())).willReturn(response);

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.loginId").value("newuser"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void signup_duplicateLoginId_returns409() throws Exception {
        given(memberService.signup(any())).willThrow(new DuplicateLoginIdException());

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupBody())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void signup_duplicateEmail_returns409() throws Exception {
        given(memberService.signup(any())).willThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupBody())))
                .andExpect(status().isConflict());
    }

    @Test
    void signup_weakPassword_returns400() throws Exception {
        Map<String, Object> body = validSignupBody();
        body.put("password", "weak");

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getMyInfo_success() throws Exception {
        MemberInfoResponse info = MemberInfoResponse.builder()
                .userId(1L)
                .loginId("testuser")
                .name("홍길동")
                .email("test@test.com")
                .phone("01012345678")
                .birthDate(LocalDate.of(1995, 1, 1))
                .gender("M")
                .createdAt(LocalDateTime.now())
                .build();
        given(memberService.getMyInfo(1L)).willReturn(info);

        mockMvc.perform(get("/api/member/me")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    void getMyInfo_unauthorized_returns403() throws Exception {
        mockMvc.perform(get("/api/member/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateMyInfo_success() throws Exception {
        Map<String, Object> body = Map.of("name", "김철수", "phone", "01099999999");

        mockMvc.perform(put("/api/member/me")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void withdraw_success() throws Exception {
        Map<String, Object> body = Map.of("reason", "개인 사정");

        mockMvc.perform(delete("/api/member/me")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
