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
import com.sw.sw_ai_doc.global.exception.*;
import com.sw.sw_ai_doc.global.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(InvalidPasswordException::new);

        if (user.isLocked()) {
            throw new AccountLockedException();
        }
        if (user.getStatus() == com.sw.sw_ai_doc.domain.member.entity.UserStatus.WITHDRAWN) {
            throw new InvalidPasswordException();
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getLoginId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        String tokenHash = hashToken(refreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getUserId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        addRefreshCookie(response, refreshToken);
        return new LoginResponseDto(accessToken, user.getName());
    }

    @Override
    public void logout(Long userId, String refreshToken) {
        if (refreshToken == null) return;
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(RefreshToken::revoke);
    }

    @Override
    public TokenRefreshResponseDto refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new com.sw.sw_ai_doc.global.exception.InvalidPasswordException();
        }
        String tokenHash = hashToken(refreshToken);
        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidPasswordException());

        if (tokenEntity.isRevoked() || tokenEntity.isExpired()) {
            throw new InvalidPasswordException();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getLoginId());

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        String newHash = hashToken(newRefreshToken);
        tokenEntity.revoke();

        RefreshToken newTokenEntity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(newHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newTokenEntity);
        addRefreshCookie(response, newRefreshToken);

        return new TokenRefreshResponseDto(newAccessToken);
    }

    @Override
    public void verifyPassword(Long userId, VerifyPasswordRequestDto request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(cookie);
    }
}
