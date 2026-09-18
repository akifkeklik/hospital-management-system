package com.hospital.appointmentsystem.department.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  📋 DEPARTMENT SERVICE — Servis Arayüzü (Interface)             ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 SERVICE INTERFACE NEDİR?                                     ║
 * ║                                                                  ║
 * ║  Interface (arayüz), "ne yapılacağını" tanımlar ama             ║
 * ║  "nasıl yapılacağını" SÖYLEMEZ.                                 ║
 * ║                                                                  ║
 * ║  Bunu bir SÖZLEŞME gibi düşün:                                  ║
 * ║  "Department servisi şu işlemleri yapabilmelidir:                ║
 * ║   - Bölüm oluştur                                              ║
 * ║   - Bölüm getir                                                 ║
 * ║   - Bölüm güncelle                                              ║
 * ║   - Bölüm sil"                                                  ║
 * ║                                                                  ║
 * ║  Nasıl yapılacağı → ServiceImpl sınıfında yazılır              ║
 * ║                                                                  ║
 * ║  Neden interface kullanıyoruz?                                   ║
 * ║  1. Soyutlama → Controller, servisin iç detaylarını bilmez     ║
 * ║  2. Değiştirilebilirlik → İmplementasyonu değiştirebilirsin     ║
 * ║  3. Test kolaylığı → Mock (sahte) implementasyon yapabilirsin   ║
 * ║                                                                  ║
 * ║  📌 api PAKETINDE OLMASI                                        ║
 * ║  Bu interface "dış dünyaya açık sözleşmedir".                   ║
 * ║  Controller bu interface'e bağımlıdır, impl'e değil.           ║
 * ║  Böylece impl değişse bile Controller etkilenmez.               ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
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
