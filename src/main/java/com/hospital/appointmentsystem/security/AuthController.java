
package com.hospital.appointmentsystem.security;

import com.hospital.appointmentsystem.user.api.UserService;
import com.hospital.appointmentsystem.patient.api.PatientService;
import com.hospital.appointmentsystem.patient.api.PatientDto;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.hospital.appointmentsystem.security.passwordreset.PasswordResetService;
import com.hospital.appointmentsystem.security.passwordreset.PasswordResetService.PasswordResetException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @org.springframework.beans.factory.annotation.Value("${cookie.secure:true}")
    private boolean cookieSecure;

    @org.springframework.beans.factory.annotation.Value("${cookie.sameSite:None}")
    private String cookieSameSite;

    @org.springframework.beans.factory.annotation.Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final AccountRegistrationService accountRegistrationService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UserDetailsService userDetailsService, UserService userService,
            PatientService patientService, DoctorService doctorService,
            UserRepository userRepository,
            AccountRegistrationService accountRegistrationService,
            PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.userRepository = userRepository;
        this.accountRegistrationService = accountRegistrationService;
        this.passwordResetService = passwordResetService;
    }

    public record AuthRequest(String username, String password) {
    }

    public record AuthResponse(boolean needsPasswordChange, String role) {
    }

    public record RegisterRequest(
            @NotBlank(message = "TC Kimlik No zorunludur.") @Pattern(regexp = "[0-9]{11}", message = "TC Kimlik No 11 haneli olmalıdır.") String tcIdentityNumber,
            @NotBlank(message = "Ad zorunludur.") String firstName,
            @NotBlank(message = "Soyad zorunludur.") String lastName,
            @NotBlank(message = "E-posta zorunludur.") @Email(message = "Geçerli bir e-posta adresi giriniz.") String email,
            String phoneNumber,
            @NotBlank(message = "Şifre zorunludur.") @Size(min = 6, message = "Şifreniz en az 6 karakter olmalıdır.") String password) {
    }

    public record DoctorRegisterRequest(
            @NotBlank(message = "TC Kimlik No zorunludur.") @Pattern(regexp = "[0-9]{11}", message = "TC Kimlik No 11 haneli olmalıdır.") String tcIdentityNumber,
            @NotBlank(message = "Ad zorunludur.") String firstName,
            @NotBlank(message = "Soyad zorunludur.") String lastName,
            @NotBlank(message = "E-posta zorunludur.") @Email(message = "Geçerli bir e-posta adresi giriniz.") String email,
            String phoneNumber,
            @NotNull(message = "Bölüm seçimi zorunludur.") Long departmentId,
            String specialization) {
    }

    public record ForgotPasswordRequest(String tcIdentityNumber, String email) {
    }

    public record ResetPasswordRequest(String token, String newPassword) {
    }

    public record MessageResponse(String message) {
    }

    public record UserProfileDto(Long id, String username, String email, String role, String firstName, String lastName,
            String phoneNumber) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse("Giriş bilgileri hatalı."));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());

        // Rol ve Referans Id'yi veritabanından çekelim
        User user = userRepository.findByUsername(authRequest.username()).orElseThrow();

        final String jwt = jwtUtil.generateToken(userDetails, user.getRole(), user.getReferenceId());

        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(jwtExpirationMs / 1000) // JWT ömrüyle senkron
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new AuthResponse(user.getNeedsPasswordChange() != null ? user.getNeedsPasswordChange() : false,
                        user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        org.springframework.http.ResponseCookie deleteCookie = org.springframework.http.ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // Silinmesi için 0 verilir
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new MessageResponse("Başarıyla çıkış yapıldı."));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        accountRegistrationService.registerPatient(request.tcIdentityNumber(), request.firstName(),
                request.lastName(), request.email(), request.phoneNumber(), request.password());
        return ResponseEntity.ok(new MessageResponse("Kullanıcı başarıyla kaydedildi."));
    }

    @PostMapping("/doctor-register")
    public ResponseEntity<MessageResponse> registerDoctor(@Valid @RequestBody DoctorRegisterRequest request) {
        accountRegistrationService.submitDoctorRegistration(new DoctorRegistrationService.SubmitCommand(
                request.tcIdentityNumber(), request.firstName(), request.lastName(), request.email(),
                request.phoneNumber(), request.departmentId(), request.specialization()));
        return ResponseEntity.ok(
                new MessageResponse("Kayıt isteğiniz alınmıştır. Yönetici onayından sonra giriş yapabileceksiniz."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.tcIdentityNumber() == null || request.email() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("TC Kimlik No ve Email zorunludur."));
        }
        
        // This method will handle both valid/invalid cases quietly to prevent user enumeration
        passwordResetService.initiatePasswordReset(request.tcIdentityNumber(), request.email());
        
        return ResponseEntity.ok(new MessageResponse("Eğer bilgileriniz sistemimizde kayıtlıysa, şifre sıfırlama bağlantısı e-posta adresinize gönderilmiştir."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.completePasswordReset(request.token(), request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Şifreniz başarıyla sıfırlandı. Giriş yapabilirsiniz."));
        } catch (PasswordResetException e) {
            String errorMsg = switch (e.getErrorCode()) {
                case "invalid_token" -> "Geçersiz token.";
                case "expired_token" -> "Token süresi dolmuş.";
                case "used_token" -> "Bu token daha önce kullanılmış.";
                case "weak_password" -> "Şifre en az 6 karakter olmalıdır.";
                default -> "Şifre sıfırlama hatası.";
            };
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: " + errorMsg));
        }
    }

    public record ForceChangePasswordRequest(String tcIdentityNumber, String oldPassword, String newPassword) {
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @PostMapping("/force-change-password")
    public ResponseEntity<?> forceChangePassword(@RequestBody ForceChangePasswordRequest request, Principal principal) {
        if (request == null || principal == null || request.tcIdentityNumber() == null
                || !principal.getName().equals(request.tcIdentityNumber())) {
            return ResponseEntity.status(403).body(new MessageResponse("Bu işlem için yetkiniz bulunmamaktadır."));
        }
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Hata: Yeni şifreniz en az 6 karakter olmalıdır."));
        }
        if (request.oldPassword() == null || request.oldPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Mevcut/Geçici şifrenizi girmelisiniz."));
        }

        User user = userRepository.findByUsername(request.tcIdentityNumber()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getNeedsPasswordChange())) {
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Hata: Bu kullanıcı için şifre sıfırlama zorunluluğu bulunmuyor veya kullanıcı geçersiz."));
        }

        boolean success = userService.changePasswordWithOld(request.tcIdentityNumber(), request.oldPassword(),
                request.newPassword());
        if (success) {
            return ResponseEntity
                    .ok(new MessageResponse("Şifreniz başarıyla güncellendi! Lütfen yeni şifrenizle giriş yapın."));
        } else {
            return ResponseEntity.status(401).body(new MessageResponse("Hata: Mevcut/Geçici şifreniz hatalı."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getName()));

        if ("ROLE_PATIENT".equals(user.getRole())) {
            PatientDto patient = patientService.getPatientById(user.getReferenceId());
            return ResponseEntity.ok(new UserProfileDto(
                    patient.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                    patient.getFirstName(), patient.getLastName(), patient.getPhoneNumber()));
        } else if ("ROLE_DOCTOR".equals(user.getRole())) {
            DoctorDto doctor = doctorService.getDoctorById(user.getReferenceId());
            return ResponseEntity.ok(new UserProfileDto(
                    doctor.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                    doctor.getFirstName(), doctor.getLastName(), doctor.getPhoneNumber()));
        }

        // Admin veya diğer roller için
        return ResponseEntity.ok(new UserProfileDto(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                "Sistem", "Yöneticisi", ""));
    }
}
