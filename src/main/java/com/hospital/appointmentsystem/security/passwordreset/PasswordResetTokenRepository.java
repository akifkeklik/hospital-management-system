package com.hospital.appointmentsystem.security.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Kullanicinin hala aktif (kullanilmamis) tokenlarini getirir. */
    List<PasswordResetToken> findAllByUserIdAndUsedAtIsNull(Long userId);

    /**
     * Yeni reset talebi geldigi zaman eski aktif tokenlari invalidate etmek icin
     * toplu update yapariz (usedAt = now).
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.userId = :userId AND t.usedAt IS NULL")
    int invalidateAllActiveTokensForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
