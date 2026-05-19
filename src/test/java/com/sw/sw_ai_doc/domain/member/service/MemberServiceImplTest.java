package com.sw.sw_ai_doc.domain.member.service;

import com.sw.sw_ai_doc.domain.member.dto.request.MemberUpdateRequest;
import com.sw.sw_ai_doc.domain.member.dto.request.SignupRequest;
import com.sw.sw_ai_doc.domain.member.dto.request.WithdrawRequest;
import com.sw.sw_ai_doc.domain.member.dto.response.MemberInfoResponse;
import com.sw.sw_ai_doc.domain.member.dto.response.SignupResponse;
import com.sw.sw_ai_doc.domain.member.entity.User;
import com.sw.sw_ai_doc.domain.member.entity.UserStatus;
import com.sw.sw_ai_doc.domain.member.repository.RefreshTokenRepository;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.DuplicateEmailException;
import com.sw.sw_ai_doc.global.exception.DuplicateLoginIdException;
import com.sw.sw_ai_doc.global.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User buildActiveUser(Long id) {
        User user = User.builder()
                .loginId("testuser")
                .password("encoded")
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

    private SignupRequest buildSignupRequest() {
        SignupRequest req = new SignupRequest();
        ReflectionTestUtils.setField(req, "loginId", "newuser");
        ReflectionTestUtils.setField(req, "password", "Password1!");
        ReflectionTestUtils.setField(req, "name", "홍길동");
        ReflectionTestUtils.setField(req, "email", "new@test.com");
        ReflectionTestUtils.setField(req, "phone", "01099999999");
        ReflectionTestUtils.setField(req, "birthDate", LocalDate.of(1995, 1, 1));
        ReflectionTestUtils.setField(req, "gender", "M");
        return req;
    }

    @Test
    void signup_success() {
        SignupRequest req = buildSignupRequest();
        User savedUser = buildActiveUser(1L);
        given(userRepository.existsByLoginId("newuser")).willReturn(false);
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encodedPw");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        SignupResponse response = memberService.signup(req);

        assertThat(response.getLoginId()).isEqualTo("testuser");
        verify(passwordEncoder).encode("Password1!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_duplicateLoginId_throwsException() {
        SignupRequest req = buildSignupRequest();
        given(userRepository.existsByLoginId("newuser")).willReturn(true);

        assertThatThrownBy(() -> memberService.signup(req))
                .isInstanceOf(DuplicateLoginIdException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_duplicateEmail_throwsException() {
        SignupRequest req = buildSignupRequest();
        given(userRepository.existsByLoginId("newuser")).willReturn(false);
        given(userRepository.existsByEmail("new@test.com")).willReturn(true);

        assertThatThrownBy(() -> memberService.signup(req))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getMyInfo_success() {
        User user = buildActiveUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        MemberInfoResponse response = memberService.getMyInfo(1L);

        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void getMyInfo_userNotFound_throwsException() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMyInfo(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getMyInfo_withdrawnUser_throwsException() {
        User withdrawn = buildActiveUser(1L);
        ReflectionTestUtils.setField(withdrawn, "status", UserStatus.WITHDRAWN);
        given(userRepository.findById(1L)).willReturn(Optional.of(withdrawn));

        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateMyInfo_withNewPassword_encodesPassword() {
        User user = buildActiveUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("NewPass1!")).willReturn("encodedNew");

        MemberUpdateRequest req = new MemberUpdateRequest();
        ReflectionTestUtils.setField(req, "name", "김철수");
        ReflectionTestUtils.setField(req, "phone", "01011111111");
        ReflectionTestUtils.setField(req, "newPassword", "NewPass1!");

        memberService.updateMyInfo(1L, req);

        verify(passwordEncoder).encode("NewPass1!");
    }

    @Test
    void withdraw_success_revokesAllTokens() {
        User user = buildActiveUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        WithdrawRequest req = new WithdrawRequest();
        ReflectionTestUtils.setField(req, "reason", "개인 사정");

        memberService.withdraw(1L, req);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}
