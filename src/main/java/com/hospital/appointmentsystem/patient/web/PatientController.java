package com.hospital.appointmentsystem.patient.web;

import com.hospital.appointmentsystem.patient.api.PatientDto;
import com.hospital.appointmentsystem.patient.api.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 🌐 Patient Controller — Hasta REST API Endpoint'leri
 *
 * Department Controller ile aynı pattern'i takip eder.
 * Artık bu yapıya aşina olmalısın! 😊
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // POST /api/patients — Yeni hasta oluştur
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {

        PatientDto dto = mapRequestToDto(request);
        PatientDto createdDto = patientService.createPatient(dto);
        PatientResponse response = mapDtoToResponse(createdDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET /api/patients/{id} — ID ile hasta getir
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientOwner(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {

        PatientDto dto = patientService.getPatientById(id);
        PatientResponse response = mapDtoToResponse(dto);

        return ResponseEntity.ok(response);
    }

    // GET /api/patients — Tüm hastaları listele
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(Pageable pageable) {

        Page<PatientDto> dtos = patientService.getAllPatients(pageable);
        Page<PatientResponse> responses = dtos.map(this::mapDtoToResponse);

        return ResponseEntity.ok(responses);
    }

    // PUT /api/patients/{id} — Hasta güncelle
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {

        PatientDto dto = mapRequestToDto(request);
        PatientDto updatedDto = patientService.updatePatient(id, dto);
        PatientResponse response = mapDtoToResponse(updatedDto);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/patients/{id} — Hasta sil
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {

        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ── Dönüşüm Metotları ──

    private PatientDto mapRequestToDto(PatientRequest request) {
        PatientDto dto = new PatientDto();
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setTcIdentityNumber(request.getTcIdentityNumber());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setEmail(request.getEmail());
        return dto;
    }

    private PatientResponse mapDtoToResponse(PatientDto dto) {
        PatientResponse response = new PatientResponse();
        response.setId(dto.getId());
        response.setFirstName(dto.getFirstName());
        response.setLastName(dto.getLastName());
        response.setTcIdentityNumber(dto.getTcIdentityNumber());
        response.setPhoneNumber(dto.getPhoneNumber());
        response.setEmail(dto.getEmail());
        return response;
    }
}
