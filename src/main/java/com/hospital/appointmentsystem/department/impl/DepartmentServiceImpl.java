package com.hospital.appointmentsystem.department.impl;

import com.hospital.appointmentsystem.department.api.DepartmentDto;
import com.hospital.appointmentsystem.department.api.DepartmentService;
import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  ⚙️ DEPARTMENT SERVICE IMPL — Servis İmplementasyonu            ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 SERVICE IMPL NEDİR?                                         ║
 * ║  Service interface'inde tanımlanan metotların GERÇEK             ║
 * ║  kodunun yazıldığı sınıftır.                                    ║
 * ║                                                                  ║
 * ║  Interface: "Bölüm oluştur" dedi (NE yapılacak)                ║
 * ║  ServiceImpl: "Bölüm şöyle oluşturulur..." yazar (NASIL)       ║
 * ║                                                                  ║
 * ║  📌 İŞ MANTIĞI (Business Logic)                                 ║
 * ║  Tüm iş kuralları burada yazılır:                               ║
 * ║  - Aynı isimde bölüm var mı kontrolü                           ║
 * ║  - Entity ↔ DTO dönüşümü                                       ║
 * ║  - Hata fırlatma                                                 ║
 * ║                                                                  ║
 * ║  📌 KATMAN MİMARİSİ                                             ║
 * ║  Controller → Service (interface) → ServiceImpl → Repository    ║
 * ║  Controller iş mantığını bilmez, Service'e sorar.               ║
 * ║  Service veritabanını bilmez, Repository'ye sorar.              ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */

// ──────────────────────────────────────────────────────────────
// 📌 @Service
// Spring'e der ki: "Bu sınıf bir SERVİS bileşenidir, yönet!"
// Spring bu sınıfı otomatik oluşturur ve gerekli yerlere enjekte eder.
// ──────────────────────────────────────────────────────────────
@Service
public class DepartmentServiceImpl implements DepartmentService {

    // ──────────────────────────────────────────────────────────
    // 📌 DEPENDENCY INJECTION (Bağımlılık Enjeksiyonu)
    //
    // Bu çok önemli bir kavram!
    //
    // DepartmentServiceImpl, veritabanı işlemi yapmak için
    // DepartmentRepository'ye İHTİYAÇ DUYAR.
    //
    // Peki Repository objesini kim oluşturuyor?
    // → SPRING oluşturuyor ve otomatik veriyor!
    //
    // Nasıl?
    // 1. Spring, @Repository olan sınıfları otomatik oluşturur
    // 2. Constructor'da parametre olarak istediğimizde,
    //    Spring "Aa, bu Repository'ye ihtiyacın var, al sana!"
    //    diyerek otomatik verir.
    //
    // Buna "Constructor Injection" denir.
    // (En temiz ve önerilen yöntemdir)
    // ──────────────────────────────────────────────────────────

    private final DepartmentRepository departmentRepository;

    // Spring bu constructor'ı çağırarak Repository'yi otomatik verir
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // ──────────────────────────────────────────────────────────
    // 📌 CREATE — Yeni Bölüm Oluştur
    // ──────────────────────────────────────────────────────────
    @Override
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {

        // 1. İş kuralı: Aynı isimde bölüm var mı kontrol et
        if (departmentRepository.existsByName(departmentDto.getName())) {
            throw new BusinessRuleException(
                    "Bu isimde bir bölüm zaten var: " + departmentDto.getName()
            );
        }

        // 2. DTO → Entity dönüşümü
        //    Neden? → Repository (veritabanı) Entity ile çalışır, DTO ile değil
        Department department = mapToEntity(departmentDto);

        // 3. Veritabanına kaydet
        //    save() metodu → JpaRepository'den geliyor (biz yazmadık!)
        //    Kayıt başarılı olursa, ID otomatik atanır
        Department savedDepartment = departmentRepository.save(department);

        // 4. Entity → DTO dönüşümü
        //    Neden? → Dış dünyaya Entity değil DTO döndürüyoruz
        return mapToDto(savedDepartment);
    }

    // ──────────────────────────────────────────────────────────
    // 📌 READ (by ID) — ID ile Bölüm Getir
    // ──────────────────────────────────────────────────────────
    @Override
    @Cacheable(value = "departments", key = "#id")
    public DepartmentDto getDepartmentById(Long id) {

        // findById() → JpaRepository'den geliyor
        // Optional<Department> döner → "Belki var, belki yok" demek
        //
        // .orElseThrow() → Eğer yoksa hata fırlat
        // Yani: "ID=5 olan bölümü bul, bulamazsan hata ver!"
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", id
                ));

        return mapToDto(department);
    }

    // ──────────────────────────────────────────────────────────
    // 📌 READ (all) — Tüm Bölümleri Getir
    // ──────────────────────────────────────────────────────────
    @Override
    @Cacheable(value = "departments")
    public Page<DepartmentDto> getAllDepartments(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(pageable);
        return departments.map(this::mapToDto);
    }

    // ──────────────────────────────────────────────────────────
    // 📌 UPDATE — Bölüm Güncelle
    // ──────────────────────────────────────────────────────────
    @Override
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {

        // 1. Önce mevcut bölümü bul (yoksa hata)
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", id
                ));

        // 2. Alanları güncelle
        existingDepartment.setName(departmentDto.getName());
        existingDepartment.setDescription(departmentDto.getDescription());

        // 3. Kaydet
        //    save() metodu: ID varsa GÜNCELLER, yoksa YENİ OLUŞTURUR
        //    Bu entity'nin ID'si var (veritabanından çektik), o yüzden günceller
        Department updatedDepartment = departmentRepository.save(existingDepartment);

        return mapToDto(updatedDepartment);
    }

    // ──────────────────────────────────────────────────────────
    // 📌 DELETE — Bölüm Sil
    // ──────────────────────────────────────────────────────────
    @Override
    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Long id) {

        // Önce var mı kontrol et
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", id
                ));

        // deleteById() → JpaRepository'den geliyor
        departmentRepository.deleteById(id);
    }

    // ══════════════════════════════════════════════════════════
    //  🔄 DÖNÜŞÜM METOTLARI (Mapping)
    //  Entity ↔ DTO arasında dönüşüm yapar
    // ══════════════════════════════════════════════════════════

    /**
     * Entity → DTO dönüşümü
     * Veritabanından gelen veriyi dış dünyaya uygun formata çevirir
     */
    private DepartmentDto mapToDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        return dto;
    }

    /**
     * DTO → Entity dönüşümü
     * Dışarıdan gelen veriyi veritabanına kaydetmeye uygun formata çevirir
     */
    private Department mapToEntity(DepartmentDto dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        return department;
    }
}
