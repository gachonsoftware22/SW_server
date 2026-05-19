package com.sw.sw_ai_doc.domain.member.repository;

import com.sw.sw_ai_doc.domain.member.entity.RefreshToken;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager em;

    private RefreshToken buildToken(Long userId, String hash) {
        return RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    void findByTokenHash_found() {
        em.persist(buildToken(1L, "hash-abc-123"));
        em.flush();

        Optional<RefreshToken> result = refreshTokenRepository.findByTokenHash("hash-abc-123");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
    }

    @Test
    void findByTokenHash_notFound() {
        Optional<RefreshToken> result = refreshTokenRepository.findByTokenHash("nonexistent-hash");

        assertThat(result).isEmpty();
    }

    @Test
    void revokeAllByUserId_setsRevokedTrue() {
        em.persist(buildToken(10L, "hash-token-1"));
        em.persist(buildToken(10L, "hash-token-2"));
        em.persist(buildToken(99L, "hash-other-user"));
        em.flush();

        refreshTokenRepository.revokeAllByUserId(10L);
        em.flush();
        em.clear();

        List<RefreshToken> userTokens = refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(10L))
                .toList();
        List<RefreshToken> otherTokens = refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(99L))
                .toList();

        assertThat(userTokens).allMatch(RefreshToken::isRevoked);
        assertThat(otherTokens).noneMatch(RefreshToken::isRevoked);
    }

    @Test
    void isExpired_whenPastExpiresAt() {
        RefreshToken expiredToken = RefreshToken.builder()
                .userId(1L)
                .tokenHash("expired-hash")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        assertThat(expiredToken.isExpired()).isTrue();
    }

    @Test
    void isExpired_whenFutureExpiresAt() {
        RefreshToken validToken = buildToken(1L, "valid-hash");

        assertThat(validToken.isExpired()).isFalse();
    }
}
