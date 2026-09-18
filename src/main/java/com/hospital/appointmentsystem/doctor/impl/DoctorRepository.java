package com.hospital.appointmentsystem.doctor.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 🗄️ Doctor Repository — Doktor veritabanı işlemleri.
 *
 * 📌 YENİ KAVRAM: İlişki Tabanlı Sorgular
 * findByDepartmentId → doktorları bölüm ID'sine göre filtrele
 * Spring, "Department" ilişkisinin "Id" alanını kullanarak SQL üretir:
 * SELECT * FROM doctors WHERE department_id = ?
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @EntityGraph(attributePaths = {"department", "polyclinic"})
    Page<Doctor> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "polyclinic"})
    Optional<Doctor> findById(Long id);

    // Belirli bir bölümdeki tüm doktorları getir
    // "Department" → ilişki adı, "Id" → Department'ın id alanı
    // SQL: SELECT * FROM doctors WHERE department_id = ?
    @EntityGraph(attributePaths = {"department", "polyclinic"})
    List<Doctor> findByDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"department", "polyclinic"})
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Doctor d WHERE CONCAT(d.firstName, ' ', d.lastName) ILIKE CONCAT('%', :query, '%') OR d.specialization ILIKE CONCAT('%', :query, '%')")
    Page<Doctor> searchDoctors(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT d.department.name, COUNT(d) FROM Doctor d GROUP BY d.department.name")
    List<Object[]> getDoctorDistribution();
}
