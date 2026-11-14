package com.hospital.appointmentsystem.department.web;

import com.hospital.appointmentsystem.department.api.DepartmentDto;
import com.hospital.appointmentsystem.department.api.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    // Service'e bağımlılık — Constructor Injection
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        // 1. Request → DTO dönüşümü
        DepartmentDto dto = mapRequestToDto(request);

        // 2. Service'e gönder (iş mantığı orada)
        DepartmentDto createdDto = departmentService.createDepartment(dto);

        // 3. DTO → Response dönüşümü
        DepartmentResponse response = mapDtoToResponse(createdDto);

        // 4. HTTP 201 (Created) durum kodu ile yanıt dön
        //    201 → "Yeni kaynak başarıyla oluşturuldu" anlamına gelir
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        DepartmentDto dto = departmentService.getDepartmentById(id);
        DepartmentResponse response = mapDtoToResponse(dto);

        // HTTP 200 (OK) ile yanıt dön
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DepartmentResponse>> getAllDepartments(Pageable pageable) {

        Page<DepartmentDto> dtos = departmentService.getAllDepartments(pageable);
        Page<DepartmentResponse> responses = dtos.map(this::mapDtoToResponse);

        return ResponseEntity.ok(responses);
    }

    // GET /api/departments/search — Bölümlerde ara
    @GetMapping("/search")
    public ResponseEntity<Page<DepartmentResponse>> searchDepartments(
            @RequestParam String query,
            Pageable pageable) {

        Page<DepartmentDto> dtos = departmentService.searchDepartments(query, pageable);
        Page<DepartmentResponse> responses = dtos.map(this::mapDtoToResponse);

        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentDto dto = mapRequestToDto(request);
        DepartmentDto updatedDto = departmentService.updateDepartment(id, dto);
        DepartmentResponse response = mapDtoToResponse(updatedDto);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {

        departmentService.deleteDepartment(id);

        // 204 No Content — Başarılı silme işlemi
        return ResponseEntity.noContent().build();
    }

    private DepartmentDto mapRequestToDto(DepartmentRequest request) {
        DepartmentDto dto = new DepartmentDto();
        dto.setName(request.getName());
        dto.setDescription(request.getDescription());
        return dto;
    }

    private DepartmentResponse mapDtoToResponse(DepartmentDto dto) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(dto.getId());
        response.setName(dto.getName());
        response.setDescription(dto.getDescription());
        return response;
    }
}
