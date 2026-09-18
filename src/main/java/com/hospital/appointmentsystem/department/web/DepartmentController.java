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

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  🌐 DEPARTMENT CONTROLLER — REST API Endpoint'leri              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 CONTROLLER NEDİR?                                           ║
 * ║  Controller, dış dünyadan gelen HTTP isteklerini karşılar.      ║
 * ║  "Birisi /api/departments adresine istek attığında ne yapayım?" ║
 * ║  sorusunun cevabıdır.                                           ║
 * ║                                                                  ║
 * ║  📌 REST API NEDİR?                                             ║
 * ║  REST, web servislerinin standart iletişim yöntemidir.          ║
 * ║  HTTP metotlarını kullanarak işlem yapar:                        ║
 * ║                                                                  ║
 * ║  ┌──────────┬────────────────────┬──────────────────────┐       ║
 * ║  │ HTTP     │ Anlamı             │ Örnek                 │       ║
 * ║  │ Metodu   │                    │                       │       ║
 * ║  ├──────────┼────────────────────┼──────────────────────┤       ║
 * ║  │ GET      │ Veri getir (OKU)   │ GET /api/departments  │       ║
 * ║  │ POST     │ Yeni oluştur       │ POST /api/departments │       ║
 * ║  │ PUT      │ Güncelle           │ PUT /api/departments/1│       ║
 * ║  │ DELETE   │ Sil                │ DELETE /api/depts/1   │       ║
 * ║  └──────────┴────────────────────┴──────────────────────┘       ║
 * ║                                                                  ║
 * ║  📌 VERİ AKIŞI                                                  ║
 * ║  İstemci → Request → Controller → Service → Repository → DB    ║
 * ║  DB → Repository → Service → Controller → Response → İstemci   ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */

// ──────────────────────────────────────────────────────────────
// 📌 @RestController
// İki anotasyonun birleşimidir:
//   @Controller → HTTP isteklerini karşılar
//   @ResponseBody → Dönüş değerini otomatik JSON'a çevirir
//
// Yani bu sınıftaki metotların döndürdüğü objeler
// otomatik olarak JSON formatına çevrilir!
// ──────────────────────────────────────────────────────────────
@RestController

// ──────────────────────────────────────────────────────────────
// 📌 @RequestMapping("/api/departments")
// Bu controller'daki TÜM endpoint'lerin önüne
// "/api/departments" eklenir.
//
// Yani @GetMapping → GET /api/departments
//      @PostMapping → POST /api/departments
// ──────────────────────────────────────────────────────────────
@RequestMapping("/api/departments")
public class DepartmentController {

    // Service'e bağımlılık — Constructor Injection
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // ──────────────────────────────────────────────────────────
    // 📌 POST /api/departments — Yeni Bölüm Oluştur
    //
    // @PostMapping → POST metodu ile gelen istekleri karşılar
    // @RequestBody → JSON gövdesini Java objesine çevirir
    //
    // İstemci şunu gönderir:
    // POST http://localhost:8080/api/departments
    // Body: { "name": "Cardiology", "description": "Heart..." }
    //
    // ResponseEntity → HTTP yanıtını kontrol etmemizi sağlar
    //   - Durum kodu (201 Created, 200 OK, 404 Not Found...)
    //   - Yanıt gövdesi (Response objesi)
    // ──────────────────────────────────────────────────────────
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

    // ──────────────────────────────────────────────────────────
    // 📌 GET /api/departments/{id} — ID ile Bölüm Getir
    //
    // @GetMapping("/{id}") → URL'deki {id} değerini yakalar
    // @PathVariable → URL'deki değişkeni metot parametresine bağlar
    //
    // Örnek: GET /api/departments/3
    //        → id = 3 olarak gelir
    // ──────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        DepartmentDto dto = departmentService.getDepartmentById(id);
        DepartmentResponse response = mapDtoToResponse(dto);

        // HTTP 200 (OK) ile yanıt dön
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────
    // 📌 GET /api/departments — Tüm Bölümleri Listele
    // ──────────────────────────────────────────────────────────
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

    // ──────────────────────────────────────────────────────────
    // 📌 PUT /api/departments/{id} — Bölüm Güncelle
    //
    // @PutMapping → PUT metodu ile gelen istekleri karşılar
    // Hem URL'den id alır, hem body'den yeni verileri alır
    // ──────────────────────────────────────────────────────────
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

    // ──────────────────────────────────────────────────────────
    // 📌 DELETE /api/departments/{id} — Bölüm Sil
    //
    // @DeleteMapping → DELETE metodu ile gelen istekleri karşılar
    // HTTP 204 (No Content) → "Silindi, döndürülecek veri yok"
    // ──────────────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {

        departmentService.deleteDepartment(id);

        // 204 No Content — Başarılı silme işlemi
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════
    //  🔄 DÖNÜŞÜM METOTLARI
    //  Request ↔ DTO ↔ Response arasında dönüşüm
    // ══════════════════════════════════════════════════════════

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
