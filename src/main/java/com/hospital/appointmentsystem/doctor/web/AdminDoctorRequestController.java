package com.hospital.appointmentsystem.doctor.web;

import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequest;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequestRepository;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.user.api.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctor-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorRequestController {

    private final DoctorRegistrationRequestRepository repository;
    private final DoctorService doctorService;
    private final UserService userService;

    public AdminDoctorRequestController(DoctorRegistrationRequestRepository repository, 
                                        DoctorService doctorService, 
                                        UserService userService) {
        this.repository = repository;
        this.doctorService = doctorService;
        this.userService = userService;
    }

    public record MessageResponse(String message) {}

    // Admin tüm bekleyen istekleri görür
    @GetMapping
    public ResponseEntity<List<DoctorRegistrationRequest>> getPendingRequests() {
        return ResponseEntity.ok(repository.findByStatusOrderByRequestDateDesc("PENDING"));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try {
            DoctorRegistrationRequest request = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("DoctorRegistrationRequest", "id", id));

            if (!"PENDING".equals(request.getStatus())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Bu istek zaten işlem görmüş."));
            }

            // 1. Doctor kaydını oluştur
            DoctorDto dto = new DoctorDto();
            dto.setFirstName(request.getFirstName());
            dto.setLastName(request.getLastName());
            dto.setTcIdentityNumber(request.getTcIdentityNumber());
            dto.setPhoneNumber(request.getPhoneNumber());
            dto.setEmail(request.getEmail());
            dto.setDepartmentId(request.getDepartmentId());
            dto.setSpecialization(request.getSpecialization());
            
            DoctorDto savedDoctor = doctorService.createDoctor(dto);

            // Rastgele Tek Kullanımlık Şifre (OTP) Üretimi
            String tempPassword = "DR-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            // 2. User kaydı doctorService içinde zaten oluşturulduğu için sadece güncelliyoruz
            userService.changePassword(request.getTcIdentityNumber(), tempPassword);

            // Şifre yenileme zorunluluğunu aktif et
            userService.setNeedsPasswordChange(request.getTcIdentityNumber(), true);

            // 3. İsteği APPROVED olarak güncelle
            request.setStatus("APPROVED");
            repository.save(request);

            return ResponseEntity.ok(new MessageResponse("Doktor başarıyla onaylandı. İlk giriş için geçici şifresi: " + tempPassword + " (Lütfen hekime iletiniz)"));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause.getMessage() != null) msg += " " + cause.getMessage();
                cause = cause.getCause();
            }
            if (msg.contains("Duplicate entry") || msg.contains("ConstraintViolationException")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Hata: Bu TC Kimlik Numarası veya E-Posta adresi zaten sistemde kayıtlı."));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("İşlem başarısız: " + e.getMessage()));
        }
    }

    // Admin reddeder
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        DoctorRegistrationRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorRegistrationRequest", "id", id));

        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Bu istek zaten işlem görmüş."));
        }

        request.setStatus("REJECTED");
        repository.save(request);

        return ResponseEntity.ok(new MessageResponse("Doktor isteği reddedildi."));
    }
}
