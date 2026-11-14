package com.hospital.appointmentsystem.doctor.web;

import com.hospital.appointmentsystem.doctor.api.DoctorRegistrationService;
import com.hospital.appointmentsystem.doctor.api.DoctorRegistrationService.RegistrationRequestView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctor-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorRequestController {

    private final DoctorRegistrationService registrationService;

    public AdminDoctorRequestController(DoctorRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public record MessageResponse(String message) {}

    @GetMapping
    public ResponseEntity<List<RegistrationRequestView>> getPendingRequests() {
        return ResponseEntity.ok(registrationService.getPendingRequests());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<MessageResponse> approveRequest(@PathVariable Long id) {
        registrationService.approve(id);
        return ResponseEntity.ok(new MessageResponse(
                "Doktor başarıyla onaylandı. Geçici şifre doktorun e-posta adresine gönderildi; ilk girişte değiştirmesi zorunludur."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<MessageResponse> rejectRequest(@PathVariable Long id) {
        registrationService.reject(id);
        return ResponseEntity.ok(new MessageResponse("Doktor isteği reddedildi."));
    }
}
