package com.sw.sw_ai_doc.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "test-secret-key-for-junit-testing-at-least-32-characters";
    private static final long ACCESS_EXPIRATION = 1_800_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", REFRESH_EXPIRATION);
    }

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "testUser");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getLoginId(token)).isEqualTo("testUser");
    }

    @Test
    void generateAndValidateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(2L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(2L);
    }

    @Test
    void getPrincipal_success() {
        String token = jwtTokenProvider.generateAccessToken(5L, "admin");

        UserPrincipal principal = jwtTokenProvider.getPrincipal(token);

        assertThat(principal.userId()).isEqualTo(5L);
        assertThat(principal.loginId()).isEqualTo("admin");
    }

    @Test
    void validateToken_expired() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "secret", SECRET);
        ReflectionTestUtils.setField(expiredProvider, "accessTokenExpiration", -1L);
        ReflectionTestUtils.setField(expiredProvider, "refreshTokenExpiration", REFRESH_EXPIRATION);

        String expiredToken = expiredProvider.generateAccessToken(1L, "testUser");

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validateToken_tampered() {
        String token = jwtTokenProvider.generateAccessToken(1L, "testUser");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_invalidFormat() {
        assertThat(jwtTokenProvider.validateToken("not.a.jwt")).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }
}
