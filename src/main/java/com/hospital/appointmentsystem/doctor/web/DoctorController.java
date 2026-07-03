package com.hospital.appointmentsystem.doctor.web;

import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 🌐 Doctor Controller — Doktor REST API Endpoint'leri
 *
 * 📌 YENİ ENDPOINT: GET /api/doctors/department/{departmentId}
 * Belirli bir bölümdeki doktorları listeler.
 * Örneğin: GET /api/doctors/department/1 → Kardiyoloji'deki doktorlar
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // POST /api/doctors — Yeni doktor oluştur
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorRequest request) {

        DoctorDto dto = mapRequestToDto(request);
        DoctorDto createdDto = doctorService.createDoctor(dto);
        DoctorResponse response = mapDtoToResponse(createdDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET /api/doctors/{id} — ID ile doktor getir
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {

        DoctorDto dto = doctorService.getDoctorById(id);
        DoctorResponse response = mapDtoToResponse(dto);

        return ResponseEntity.ok(response);
    }

    // GET /api/doctors — Tüm doktorları listele
    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(Pageable pageable) {

        Page<DoctorDto> dtos = doctorService.getAllDoctors(pageable);
        Page<DoctorResponse> responses = dtos.map(this::mapDtoToResponse);

        return ResponseEntity.ok(responses);
    }

    // ──────────────────────────────────────────────────────────
    // ⭐ YENİ: Bölüme göre doktor listele
    // GET /api/doctors/department/1 → ID=1 bölümündeki doktorlar
    // ──────────────────────────────────────────────────────────
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {

        List<DoctorDto> dtos = doctorService.getDoctorsByDepartmentId(departmentId);
        List<DoctorResponse> responses = dtos.stream()
                .map(this::mapDtoToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // PUT /api/doctors/{id} — Doktor güncelle
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {

        DoctorDto dto = mapRequestToDto(request);
        DoctorDto updatedDto = doctorService.updateDoctor(id, dto);
        DoctorResponse response = mapDtoToResponse(updatedDto);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/doctors/{id} — Doktor sil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {

        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ── Dönüşüm Metotları ──

    private DoctorDto mapRequestToDto(DoctorRequest request) {
        DoctorDto dto = new DoctorDto();
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setSpecialization(request.getSpecialization());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setEmail(request.getEmail());
        dto.setDepartmentId(request.getDepartmentId());
        dto.setPolyclinicId(request.getPolyclinicId());
        return dto;
    }

    private DoctorResponse mapDtoToResponse(DoctorDto dto) {
        DoctorResponse response = new DoctorResponse();
        response.setId(dto.getId());
        response.setFirstName(dto.getFirstName());
        response.setLastName(dto.getLastName());
        response.setSpecialization(dto.getSpecialization());
        response.setPhoneNumber(dto.getPhoneNumber());
        response.setEmail(dto.getEmail());
        response.setDepartmentId(dto.getDepartmentId());
        response.setDepartmentName(dto.getDepartmentName());
        response.setPolyclinicId(dto.getPolyclinicId());
        response.setPolyclinicName(dto.getPolyclinicName());
        return response;
    }
}
