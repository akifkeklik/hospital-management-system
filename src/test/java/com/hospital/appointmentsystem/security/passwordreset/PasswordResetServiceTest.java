package com.hospital.appointmentsystem.security.passwordreset;

import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                tokenRepository, userRepository, passwordEncoder, emailService
        );
        ReflectionTestUtils.setField(passwordResetService, "expirationMinutes", 30);
        ReflectionTestUtils.setField(passwordResetService, "frontendBaseUrl", "http://localhost:3000");
    }

    @Test
    void initiatePasswordReset_unknownUser_silentlyReturns() {
        when(userRepository.findByUsername("12345678901")).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset("12345678901", "test@test.com");

        verify(tokenRepository, never()).invalidateAllActiveTokensForUser(any(), any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void initiatePasswordReset_emailMismatch_silentlyReturns() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("12345678901");
        user.setEmail("real@test.com");
        when(userRepository.findByUsername("12345678901")).thenReturn(Optional.of(user));

        passwordResetService.initiatePasswordReset("12345678901", "wrong@test.com");

        verify(tokenRepository, never()).invalidateAllActiveTokensForUser(any(), any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void initiatePasswordReset_success_invalidatesOldAndSavesNewToken() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("12345678901");
        user.setEmail("real@test.com");
        when(userRepository.findByUsername("12345678901")).thenReturn(Optional.of(user));
        when(tokenRepository.invalidateAllActiveTokensForUser(eq(1L), any(Instant.class))).thenReturn(1);

        passwordResetService.initiatePasswordReset("12345678901", "real@test.com");

        verify(tokenRepository).invalidateAllActiveTokensForUser(eq(1L), any(Instant.class));
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        
        assertThat(savedToken.getUserId()).isEqualTo(1L);
        assertThat(savedToken.getTokenHash()).isNotBlank();
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.isExpired()).isFalse();

        verify(emailService).sendPasswordResetEmail(eq("real@test.com"), contains("token="));
    }

    @Test
    void completePasswordReset_nullOrBlankToken_throwsException() {
        assertThatThrownBy(() -> passwordResetService.completePasswordReset("", "newPass123"))
                .isInstanceOf(PasswordResetService.PasswordResetException.class)
                .hasMessage("invalid_token");
    }

    @Test
    void completePasswordReset_weakPassword_throwsException() {
        assertThatThrownBy(() -> passwordResetService.completePasswordReset("validTokenData", "123"))
                .isInstanceOf(PasswordResetService.PasswordResetException.class)
                .hasMessage("weak_password");
    }

    @Test
    void completePasswordReset_tokenNotFound_throwsException() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.completePasswordReset("validTokenData", "newPass123"))
                .isInstanceOf(PasswordResetService.PasswordResetException.class)
                .hasMessage("invalid_token");
    }

    @Test
    void completePasswordReset_tokenExpired_throwsException() {
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setExpiresAt(Instant.now().minusSeconds(3600));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> passwordResetService.completePasswordReset("validTokenData", "newPass123"))
                .isInstanceOf(PasswordResetService.PasswordResetException.class)
                .hasMessage("expired_token");
    }

    @Test
    void completePasswordReset_tokenUsed_throwsException() {
        PasswordResetToken usedToken = new PasswordResetToken();
        usedToken.setExpiresAt(Instant.now().plusSeconds(3600));
        usedToken.setUsedAt(Instant.now());
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> passwordResetService.completePasswordReset("validTokenData", "newPass123"))
                .isInstanceOf(PasswordResetService.PasswordResetException.class)
                .hasMessage("used_token");
    }

    @Test
    void completePasswordReset_success_updatesPasswordAndMarksTokenUsed() {
        PasswordResetToken validToken = new PasswordResetToken();
        validToken.setUserId(1L);
        validToken.setExpiresAt(Instant.now().plusSeconds(3600));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setNeedsPasswordChange(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedPassword");

        passwordResetService.completePasswordReset("validTokenData", "newPass123");

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getNeedsPasswordChange()).isFalse();

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isUsed()).isTrue();
        assertThat(savedToken.getUsedAt()).isNotNull();
    }
}
