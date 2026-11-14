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

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Spring bu constructor'ı çağırarak Repository'yi otomatik verir
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

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

    @Override
    @Cacheable(value = "departments")
    public Page<DepartmentDto> getAllDepartments(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(pageable);
        return departments.map(this::mapToDto);
    }

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

    @Override
    public Page<DepartmentDto> searchDepartments(String query, Pageable pageable) {
        Page<Department> departments = departmentRepository.searchDepartments(query, pageable);
        return departments.map(this::mapToDto);
    }

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
