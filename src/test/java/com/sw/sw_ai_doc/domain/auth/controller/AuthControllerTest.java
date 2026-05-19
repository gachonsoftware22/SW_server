package com.sw.sw_ai_doc.domain.auth.controller;

import tools.jackson.databind.ObjectMapper;
import com.sw.sw_ai_doc.domain.auth.dto.LoginResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.TokenRefreshResponseDto;
import com.sw.sw_ai_doc.domain.auth.service.AuthService;
import com.sw.sw_ai_doc.global.exception.AccountLockedException;
import com.sw.sw_ai_doc.global.exception.InvalidPasswordException;
import com.sw.sw_ai_doc.support.SecurityTestHelper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void login_success() throws Exception {
        LoginResponseDto loginResponse = new LoginResponseDto("access-token", "홍길동");
        given(authService.login(any(), any())).willReturn(loginResponse);

        String body = objectMapper.writeValueAsString(
                Map.of("loginId", "testuser", "password", "Password1!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    void login_invalidPassword_returns401() throws Exception {
        given(authService.login(any(), any())).willThrow(new InvalidPasswordException());

        String body = objectMapper.writeValueAsString(
                Map.of("loginId", "testuser", "password", "wrong"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_accountLocked_returns423() throws Exception {
        given(authService.login(any(), any())).willThrow(new AccountLockedException());

        String body = objectMapper.writeValueAsString(
                Map.of("loginId", "locked", "password", "Password1!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value(423));
    }

    @Test
    void login_blankLoginId_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("loginId", "", "password", "Password1!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(SecurityTestHelper.mockUser())
                        .cookie(new Cookie("refreshToken", "some-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void refresh_success() throws Exception {
        TokenRefreshResponseDto response = new TokenRefreshResponseDto("new-access-token");
        given(authService.refresh(anyString(), any())).willReturn(response);

        mockMvc.perform(post("/api/auth/token/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void verifyPassword_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("password", "Password1!"));

        mockMvc.perform(post("/api/auth/verify-password")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void verifyPassword_wrongPassword_returns401() throws Exception {
        doThrow(new InvalidPasswordException()).when(authService).verifyPassword(any(), any());

        String body = objectMapper.writeValueAsString(Map.of("password", "wrongPw"));

        mockMvc.perform(post("/api/auth/verify-password")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
