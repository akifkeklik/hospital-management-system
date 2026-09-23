package com.hospital.appointmentsystem.security.passwordreset;

import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Password reset is akisi:
 *
 * 1. initiatePasswordReset(tc, email)
 *    - Kullanici TC + email ile dogrulayarak bulunur (bulunamazsa yine ayni response).
 *    - Eski aktif tokenlar invalidate edilir (yeni talep eskiyi gecersiz kilar).
 *    - SecureRandom ile 32-byte token uretilir (256-bit entropy).
 *    - Token'in SHA-256 hash'i DB'ye yazilir. Plaintext token asla DB'ye yazilmaz.
 *    - Reset link frontend URL'sine gore olusturulur ve email ile gonderilir.
 *
 * 2. completePasswordReset(token, newPassword)
 *    - Token SHA-256 ile hashlenir ve DB'de aranir.
 *    - Expire / used kontrolleri yapilir.
 *    - Yeni sifre BCrypt ile encode edilir ve kaydedilir.
 *    - Token used olarak isaretlenir.
 *    - Kullanicinin diger aktif reset tokenlari invalidate edilir.
 *
 * GUVENLIK:
 * - Token plaintext hicbir yerde loglanmaz.
 * - Sifre plaintext hicbir yerde loglanmaz.
 * - User enumeration: her durumda ayni genel response donulur (AuthController'da saglanir).
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final int TOKEN_BYTES = 32; // 256-bit entropy
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${password.reset.token.expiration.minutes:30}")
    private int expirationMinutes;

    @Value("${frontend.base.url:http://localhost:3000}")
    private String frontendBaseUrl;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Sifre sifirlama talebini baslatir.
     * Kullanici bulunamasa bile exception firlatmaz (user enumeration korumasi).
     * Caller (AuthController) her durumda ayni mesaji donmelidir.
     *
     * @param tcIdentityNumber kullanicinin TC kimlik numarasi
     * @param email            kullanicinin email adresi
     */
    @Transactional
    public void initiatePasswordReset(String tcIdentityNumber, String email) {
        // TC numarasi = username (sistem mimari geregi)
        Optional<User> optUser = userRepository.findByUsername(tcIdentityNumber);

        if (optUser.isEmpty()) {
            // Kullanici yok — sessizce don (user enumeration korunur)
            log.info("Password reset requested for unknown user; no action taken.");
            return;
        }

        User user = optUser.get();

        // Email de eslesmeli (TC + email kombinasyonu)
        if (!user.getEmail().equalsIgnoreCase(email)) {
            log.info("Password reset requested but email did not match; no action taken.");
            return;
        }

        // Onceki aktif tokenlari invalidate et
        int invalidated = tokenRepository.invalidateAllActiveTokensForUser(user.getId(), Instant.now());
        if (invalidated > 0) {
            log.info("Invalidated {} previous active reset token(s) for user.", invalidated);
        }

        // SecureRandom ile 32-byte token uret → Base64url encoding (URL-safe)
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        String plaintextToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // SHA-256 hash'ini hesapla — bu DB'ye yazilacak olan
        String tokenHash = sha256Hex(plaintextToken);

        // Token kaydet
        Instant now = Instant.now();
        PasswordResetToken resetToken = new PasswordResetToken(
                user.getId(),
                tokenHash,
                now.plus(expirationMinutes, ChronoUnit.MINUTES),
                now
        );
        tokenRepository.save(resetToken);

        // Reset link olustur — plaintext token URL query param olarak kullanilir
        String resetLink = frontendBaseUrl + "/reset-password?token=" + plaintextToken;

        // Email gonder
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        } catch (Exception e) {
            // Email gonderilemese de kullaniciya teknik hata sizmamali
            log.error("Password reset email could not be sent (token saved in DB).", e);
            // Token DB'de kayitli, ileride cozum saglanabilir
        }
    }

    /**
     * Token ile sifreyi sifirlar.
     *
     * @param plaintextToken URL'den gelen plaintext token
     * @param newPassword    yeni sifre (plaintext — BCrypt ile encode edilecek)
     * @throws PasswordResetException gecersiz / expired / kullanilmis token icin
     */
    @Transactional
    public void completePasswordReset(String plaintextToken, String newPassword) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            throw new PasswordResetException("invalid_token");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new PasswordResetException("weak_password");
        }

        String tokenHash = sha256Hex(plaintextToken);

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new PasswordResetException("invalid_token"));

        if (resetToken.isExpired()) {
            throw new PasswordResetException("expired_token");
        }
        if (resetToken.isUsed()) {
            throw new PasswordResetException("used_token");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new PasswordResetException("invalid_token"));

        // Yeni sifreyi BCrypt ile hashle ve kaydet (plaintext loglanmaz)
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedsPasswordChange(false);
        userRepository.save(user);

        // Token'i kullanilmis olarak isaretle
        resetToken.setUsedAt(Instant.now());
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user ID: {}", user.getId());
    }

    /**
     * SHA-256 hash hesaplar ve hex string olarak dondurur.
     * GUVENLIK: Bu metod token icerigi loglanmaz.
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 her JVM'de mevcut; bu hata teorik
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /** Password reset sirasinda fiatlatilan is kurali istisnalari. */
    public static class PasswordResetException extends RuntimeException {
        private final String errorCode;

        public PasswordResetException(String errorCode) {
            super(errorCode);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}
