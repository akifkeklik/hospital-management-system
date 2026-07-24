package com.hospital.appointmentsystem.appointment.web;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 🌐 Appointment Controller — Randevu REST API Endpoint'leri
 *
 * En zengin Controller — hastaya göre, doktora göre sorgulama
 * ve durum güncelleme endpoint'leri var.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // POST /api/appointments — Yeni randevu oluştur
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentDto dto = mapRequestToDto(request);
        AppointmentDto createdDto = appointmentService.createAppointment(dto);
        AppointmentResponse response = mapDtoToResponse(createdDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET /api/appointments/{id} — ID ile randevu getir
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {

        AppointmentDto dto = appointmentService.getAppointmentById(id);
        AppointmentResponse response = mapDtoToResponse(dto);

        return ResponseEntity.ok(response);
    }

    // GET /api/appointments — Tüm randevuları listele
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(Pageable pageable) {

        Page<AppointmentDto> dtos = appointmentService.getAllAppointments(pageable);
        Page<AppointmentResponse> responses = dtos.map(this::mapDtoToResponse);

        return ResponseEntity.ok(responses);
    }

    // GET /api/appointments/patient/{patientId} — Hastanın randevuları
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(
            @PathVariable Long patientId) {

        List<AppointmentDto> dtos = appointmentService.getAppointmentsByPatientId(patientId);
        List<AppointmentResponse> responses = dtos.stream()
                .map(this::mapDtoToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // GET /api/appointments/doctor/{doctorId} — Doktorun randevuları
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {

        List<AppointmentDto> dtos = appointmentService.getAppointmentsByDoctorId(doctorId);
        List<AppointmentResponse> responses = dtos.stream()
                .map(this::mapDtoToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // GET /api/appointments/available-slots — Belirli doktor ve tarih için müsait
    // saatleri getir
    @GetMapping("/available-slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<String> slots = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }

    // PUT /api/appointments/{id} — Randevu güncelle
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentDto dto = mapRequestToDto(request);
        AppointmentDto updatedDto = appointmentService.updateAppointment(id, dto);
        AppointmentResponse response = mapDtoToResponse(updatedDto);

        return ResponseEntity.ok(response);
    }

    // PATCH /api/appointments/{id}/status — Sadece durum güncelle
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        String newStatus = statusUpdate.get("status");
        AppointmentDto updatedDto = appointmentService.updateAppointmentStatus(id, newStatus);
        AppointmentResponse response = mapDtoToResponse(updatedDto);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/appointments/{id} — Randevu sil
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {

        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Dönüşüm Metotları ──

    private AppointmentDto mapRequestToDto(AppointmentRequest request) {
        AppointmentDto dto = new AppointmentDto();
        dto.setPatientId(request.getPatientId());
        dto.setDoctorId(request.getDoctorId());
        dto.setAppointmentDate(request.getAppointmentDate());
        dto.setNotes(request.getNotes());
        return dto;
    }

    private AppointmentResponse mapDtoToResponse(AppointmentDto dto) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(dto.getId());
        response.setPatientId(dto.getPatientId());
        response.setPatientFullName(dto.getPatientFullName());
        response.setDoctorId(dto.getDoctorId());
        response.setDoctorFullName(dto.getDoctorFullName());
        response.setDepartmentName(dto.getDepartmentName());
        response.setAppointmentDate(dto.getAppointmentDate());
        response.setStatus(dto.getStatus());
        response.setNotes(dto.getNotes());
        return response;
    }
}