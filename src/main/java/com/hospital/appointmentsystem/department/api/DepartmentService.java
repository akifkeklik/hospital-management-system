package com.hospital.appointmentsystem.department.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    /**
     * Yeni bir bölüm oluşturur.
     *
     * @param departmentDto Oluşturulacak bölümün bilgileri
     * @return Oluşturulan bölümün bilgileri (ID dahil)
     */
    DepartmentDto createDepartment(DepartmentDto departmentDto);

    /**
     * ID'ye göre bölümü getirir.
     *
     * @param id Bölümün benzersiz kimliği
     * @return Bulunan bölümün bilgileri
     */
    DepartmentDto getDepartmentById(Long id);

    /**
     * Tüm bölümleri listeler.
     *
     * @return Bölüm listesi
     */
    Page<DepartmentDto> getAllDepartments(Pageable pageable);

    /**
     * Var olan bir bölümü günceller.
     *
     * @param id            Güncellenecek bölümün ID'si
     * @param departmentDto Yeni bölüm bilgileri
     * @return Güncellenmiş bölüm bilgileri
     */
    DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto);

    /**
     * Bölümü siler.
     *
     * @param id Silinecek bölümün ID'si
     */
    void deleteDepartment(Long id);

    Page<DepartmentDto> searchDepartments(String query, Pageable pageable);
}
