package com.sw.sw_ai_doc.domain.member.service;

import com.sw.sw_ai_doc.domain.member.dto.request.*;
import com.sw.sw_ai_doc.domain.member.dto.response.MemberInfoResponse;
import com.sw.sw_ai_doc.domain.member.dto.response.SignupResponse;
import com.sw.sw_ai_doc.domain.member.entity.User;
import com.sw.sw_ai_doc.domain.member.entity.UserStatus;
import com.sw.sw_ai_doc.domain.member.repository.RefreshTokenRepository;
import com.sw.sw_ai_doc.domain.member.repository.UserRepository;
import com.sw.sw_ai_doc.global.exception.DuplicateEmailException;
import com.sw.sw_ai_doc.global.exception.DuplicateLoginIdException;
import com.sw.sw_ai_doc.global.exception.InvalidPasswordException;
import com.sw.sw_ai_doc.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new DuplicateLoginIdException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .status(UserStatus.ACTIVE)
                .build();
        User saved = userRepository.save(user);
        return new SignupResponse(saved.getUserId(), saved.getLoginId(), "가입이 정상적으로 처리되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInfo(Long userId) {
        User user = findActiveUser(userId);
        return MemberInfoResponse.from(user);
    }

    @Override
    public void updateMyInfo(Long userId, MemberUpdateRequest request) {
        User user = findActiveUser(userId);
        if (StringUtils.hasText(request.getName())) {
            user.updateName(request.getName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.updatePhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getNewPassword())) {
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    @Override
    public void withdraw(Long userId) {
        User user = findActiveUser(userId);
        user.withdraw(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.isActive()) {
            throw new UserNotFoundException();
        }
        return user;
    }

}
