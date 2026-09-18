package com.hospital.appointmentsystem.security;

import com.hospital.appointmentsystem.user.api.UserService;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import com.hospital.appointmentsystem.security.AuthController.ForceChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthControllerSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void forceChangePassword_Success() {
        ForceChangePasswordRequest request = new ForceChangePasswordRequest("12345678901", "oldPass", "newPass");
        User mockUser = new User();
        mockUser.setNeedsPasswordChange(true);

        when(userRepository.findByUsername("12345678901")).thenReturn(Optional.of(mockUser));
        when(userService.changePasswordWithOld("12345678901", "oldPass", "newPass")).thenReturn(true);

        ResponseEntity<?> response = authController.forceChangePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).changePasswordWithOld("12345678901", "oldPass", "newPass");
    }

    @Test
    void forceChangePassword_WrongOldPassword() {
        ForceChangePasswordRequest request = new ForceChangePasswordRequest("12345678901", "wrongOld", "newPass");
        User mockUser = new User();
        mockUser.setNeedsPasswordChange(true);

        when(userRepository.findByUsername("12345678901")).thenReturn(Optional.of(mockUser));
        when(userService.changePasswordWithOld("12345678901", "wrongOld", "newPass")).thenReturn(false);

        ResponseEntity<?> response = authController.forceChangePassword(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService).changePasswordWithOld("12345678901", "wrongOld", "newPass");
    }

    @Test
    void forceChangePassword_NonExistentUser() {
        ForceChangePasswordRequest request = new ForceChangePasswordRequest("00000000000", "oldPass", "newPass");

        when(userRepository.findByUsername("00000000000")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.forceChangePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).changePasswordWithOld(anyString(), anyString(), anyString());
    }

    @Test
    void forceChangePassword_MissingOldPassword() {
        ForceChangePasswordRequest request = new ForceChangePasswordRequest("12345678901", "", "newPass");

        ResponseEntity<?> response = authController.forceChangePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).changePasswordWithOld(anyString(), anyString(), anyString());
    }

    @Test
    void forceChangePassword_InvalidNewPassword() {
        ForceChangePasswordRequest request = new ForceChangePasswordRequest("12345678901", "oldPass", "short");

        ResponseEntity<?> response = authController.forceChangePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).changePasswordWithOld(anyString(), anyString(), anyString());
    }
}
