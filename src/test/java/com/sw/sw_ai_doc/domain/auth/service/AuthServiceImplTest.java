package com.sw.sw_ai_doc.domain.auth.service;

import com.sw.sw_ai_doc.domain.auth.dto.LoginRequestDto;
import com.sw.sw_ai_doc.domain.auth.dto.LoginResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.TokenRefreshResponseDto;
import com.sw.sw_ai_doc.domain.auth.dto.VerifyPasswordRequestDto;
import com.sw.sw_ai_doc.domain.member.entity.RefreshToken;
import com.sw.sw_ai_doc.domain.member.entity.User;
import com.sw.sw_ai_doc.domain.member.entity.UserStatus;
import com.sw.sw_ai_doc.domain.member.repository.RefreshTokenRepository;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.AccountLockedException;
import com.sw.sw_ai_doc.global.exception.InvalidPasswordException;
import com.sw.sw_ai_doc.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private final HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }

    private User buildActiveUser(Long id) {
        User user = User.builder()
                .loginId("testuser")
                .password("encodedPw")
                .name("홍길동")
                .email("test@test.com")
                .phone("01012345678")
                .birthDate(LocalDate.of(1995, 1, 1))
                .gender("M")
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private LoginRequestDto buildLoginRequest(String loginId, String password) {
        LoginRequestDto req = new LoginRequestDto();
        ReflectionTestUtils.setField(req, "loginId", loginId);
        ReflectionTestUtils.setField(req, "password", password);
        return req;
    }

    @Test
    void login_success() {
        User user = buildActiveUser(1L);
        given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Password1!", "encodedPw")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(1L, "testuser")).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token");
        given(refreshTokenRepository.save(any())).willReturn(mock(RefreshToken.class));

        LoginResponseDto response = authService.login(buildLoginRequest("testuser", "Password1!"), mockResponse);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getName()).isEqualTo("홍길동");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_userNotFound_throwsInvalidPassword() {
        given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(buildLoginRequest("unknown", "pw"), mockResponse))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void login_accountLocked_throwsException() {
        User locked = buildActiveUser(1L);
        ReflectionTestUtils.setField(locked, "status", UserStatus.LOCKED);
        given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(locked));

        assertThatThrownBy(() -> authService.login(buildLoginRequest("testuser", "pw"), mockResponse))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void login_wrongPassword_throwsInvalidPassword() {
        User user = buildActiveUser(1L);
        given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        assertThatThrownBy(() -> authService.login(buildLoginRequest("testuser", "wrongPw"), mockResponse))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void logout_success_revokesToken() {
        String rawToken = "raw-refresh-token";
        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(1L)
                .tokenHash("some-hash")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(tokenEntity));

        authService.logout(1L, rawToken);

        assertThat(tokenEntity.isRevoked()).isTrue();
    }

    @Test
    void logout_nullToken_doesNotThrow() {
        authService.logout(1L, null);
    }

    @Test
    void refresh_success_issuesNewTokens() {
        String rawToken = "old-refresh-token";
        User user = buildActiveUser(1L);
        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(1L)
                .tokenHash("some-hash")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(tokenEntity));
        given(jwtTokenProvider.getUserId(rawToken)).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(1L, "testuser")).willReturn("new-access-token");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("new-refresh-token");
        given(refreshTokenRepository.save(any())).willReturn(mock(RefreshToken.class));

        TokenRefreshResponseDto response = authService.refresh(rawToken, mockResponse);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(tokenEntity.isRevoked()).isTrue();
    }

    @Test
    void refresh_nullToken_throwsException() {
        assertThatThrownBy(() -> authService.refresh(null, mockResponse))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void refresh_revokedToken_throwsException() {
        String rawToken = "revoked-token";
        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(1L)
                .tokenHash("some-hash")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .build();
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(tokenEntity));

        assertThatThrownBy(() -> authService.refresh(rawToken, mockResponse))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void refresh_expiredToken_throwsException() {
        String rawToken = "expired-token";
        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(1L)
                .tokenHash("some-hash")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(tokenEntity));

        assertThatThrownBy(() -> authService.refresh(rawToken, mockResponse))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void verifyPassword_success() {
        User user = buildActiveUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Password1!", "encodedPw")).willReturn(true);

        VerifyPasswordRequestDto req = new VerifyPasswordRequestDto();
        ReflectionTestUtils.setField(req, "password", "Password1!");

        authService.verifyPassword(1L, req);
    }

    @Test
    void verifyPassword_wrongPassword_throwsException() {
        User user = buildActiveUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        VerifyPasswordRequestDto req = new VerifyPasswordRequestDto();
        ReflectionTestUtils.setField(req, "password", "wrongPw");

        assertThatThrownBy(() -> authService.verifyPassword(1L, req))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
