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
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequest;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequestRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final DoctorRegistrationRequestRepository doctorRegistrationRequestRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                          UserDetailsService userDetailsService, UserService userService,
                          PatientService patientService, DoctorService doctorService, 
                          UserRepository userRepository, 
                          DoctorRegistrationRequestRepository doctorRegistrationRequestRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.userRepository = userRepository;
        this.doctorRegistrationRequestRepository = doctorRegistrationRequestRepository;
    }

    public record AuthRequest(String username, String password) {}
    public record AuthResponse(String token, boolean needsPasswordChange) {}
    public record RegisterRequest(String tcIdentityNumber, String firstName, String lastName, String email, String phoneNumber, String password) {}
    public record DoctorRegisterRequest(String tcIdentityNumber, String firstName, String lastName, String email, String phoneNumber, Long departmentId, String specialization) {}
    public record ResetPasswordRequest(String tcIdentityNumber, String email, String newPassword) {}
    public record MessageResponse(String message) {}
    public record UserProfileDto(Long id, String username, String email, String role, String firstName, String lastName, String phoneNumber) {}

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse("Giriş bilgileri hatalı."));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());
        
        // Rol ve Referans Id'yi veritabanından çekelim
        User user = userRepository.findByUsername(authRequest.username()).orElseThrow();
        
        final String jwt = jwtUtil.generateToken(userDetails, user.getRole(), user.getReferenceId());

        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie.from("jwt", jwt)
                .secure(true) // Production'da (HTTPS ve Cross-Origin) true ZORUNLUDUR
                .path("/")
                .maxAge(10 * 60 * 60) // 10 saat
                .sameSite("None") // Cross-Domain (Vercel -> Render) için None ZORUNLUDUR
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new AuthResponse(jwt, user.getNeedsPasswordChange() != null ? user.getNeedsPasswordChange() : false));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        org.springframework.http.ResponseCookie deleteCookie = org.springframework.http.ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true) // Production'da true
                .path("/")
                .maxAge(0) // Silinmesi için 0 verilir
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new MessageResponse("Başarıyla çıkış yapıldı."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userService.existsByUsername(registerRequest.tcIdentityNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu TC Kimlik Numarası zaten kayıtlı."));
        }
        if (userService.existsByEmail(registerRequest.email())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu e-posta adresi zaten kullanılıyor."));
        }
        if (registerRequest.password() == null || registerRequest.password().length() < 6) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Şifreniz en az 6 karakter olmalıdır."));
        }

        // Önce hastayı kaydet
        PatientDto patientDto = new PatientDto(null, registerRequest.firstName(), registerRequest.lastName(), 
                                                registerRequest.tcIdentityNumber(), registerRequest.phoneNumber(), 
                                                registerRequest.email());
        PatientDto savedPatient = patientService.createPatient(patientDto);

        // Sonra kullanıcıyı oluştur (ROLE_PATIENT) - TC Kimlik Numarasını kullanıcı adı olarak kullanıyoruz.
        userService.registerUser(registerRequest.tcIdentityNumber(), registerRequest.email(), registerRequest.password(), 
                                 "ROLE_PATIENT", savedPatient.getId());

        return ResponseEntity.ok(new MessageResponse("Kullanıcı başarıyla kaydedildi."));
    }

    @PostMapping("/doctor-register")
    public ResponseEntity<?> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        if (userService.existsByUsername(request.tcIdentityNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu TC Kimlik Numarası zaten kayıtlı."));
        }
        if (userService.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu e-posta adresi zaten kullanılıyor."));
        }

        DoctorRegistrationRequest regRequest = new DoctorRegistrationRequest();
        regRequest.setTcIdentityNumber(request.tcIdentityNumber());
        regRequest.setFirstName(request.firstName());
        regRequest.setLastName(request.lastName());
        regRequest.setEmail(request.email());
        regRequest.setPhoneNumber(request.phoneNumber());
        regRequest.setDepartmentId(request.departmentId());
        regRequest.setSpecialization(request.specialization());
        regRequest.setPassword(""); // Dummy empty password since it will be generated by Admin via OTP
        
        doctorRegistrationRequestRepository.save(regRequest);

        return ResponseEntity.ok(new MessageResponse("Kayıt isteğiniz alınmıştır. Yönetici onayından sonra giriş yapabileceksiniz."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Yeni şifreniz en az 6 karakter olmalıdır."));
        }
        
        boolean success = userService.resetPassword(request.tcIdentityNumber(), request.email(), request.newPassword());
        
        if (success) {
            return ResponseEntity.ok(new MessageResponse("Şifreniz başarıyla sıfırlandı. Yeni şifrenizle giriş yapabilirsiniz."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Girdiğiniz TC Kimlik Numarası veya E-Posta adresi sistemimizle eşleşmiyor."));
        }
    }

    public record ForceChangePasswordRequest(String tcIdentityNumber, String newPassword) {}

    @PostMapping("/force-change-password")
    public ResponseEntity<?> forceChangePassword(@RequestBody ForceChangePasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Yeni şifreniz en az 6 karakter olmalıdır."));
        }
        
        User user = userRepository.findByUsername(request.tcIdentityNumber()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getNeedsPasswordChange())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu kullanıcı için şifre sıfırlama zorunluluğu bulunmuyor veya kullanıcı geçersiz."));
        }
        
        boolean success = userService.changePassword(request.tcIdentityNumber(), request.newPassword());
        if (success) {
            return ResponseEntity.ok(new MessageResponse("Şifreniz başarıyla güncellendi! Lütfen yeni şifrenizle giriş yapın."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Hata: Şifre değiştirilemedi."));
        }
    }

    // GEÇİCİ: Geliştirme aşamasında kilitli kalan TC'leri silmek için
    @GetMapping("/force-delete/{tc}")
    public ResponseEntity<?> forceDelete(@PathVariable String tc) {
        userRepository.findByUsername(tc).ifPresent(userRepository::delete);
        return ResponseEntity.ok(new MessageResponse(tc + " numaralı kayıt veritabanından tamamen silindi. Şimdi baştan kayıt olabilirsiniz!"));
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
                patient.getFirstName(), patient.getLastName(), patient.getPhoneNumber()
            ));
        } else if ("ROLE_DOCTOR".equals(user.getRole())) {
            DoctorDto doctor = doctorService.getDoctorById(user.getReferenceId());
            return ResponseEntity.ok(new UserProfileDto(
                doctor.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                doctor.getFirstName(), doctor.getLastName(), doctor.getPhoneNumber()
            ));
        }
        
        // Admin veya diğer roller için
        return ResponseEntity.ok(new UserProfileDto(
            user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
            "Sistem", "Yöneticisi", ""
        ));
    }
}
