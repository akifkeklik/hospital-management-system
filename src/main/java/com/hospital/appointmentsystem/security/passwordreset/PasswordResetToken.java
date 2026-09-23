package com.hospital.appointmentsystem.security.passwordreset;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Password reset token entity.
 * GUVENLIK: Plaintext token hic saklanmaz; yalnizca SHA-256 hash'i tutulur.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SHA-256 hash (hex, 64 karakter). Plaintext hic bu alana yazilmaz. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** NULL => kullanilmamis. Dolu => single-use politikasi geregi gecersiz. */
    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(Long userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }
}
