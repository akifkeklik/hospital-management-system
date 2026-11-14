package com.hospital.appointmentsystem.department.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // İsme göre bölüm bul
    // Spring bu metot adını okuyup otomatik SQL üretir:
    // SELECT * FROM departments WHERE name = ?
    Department findByName(String name);

    // Bu isimde bir bölüm var mı?
    // SELECT COUNT(*) > 0 FROM departments WHERE name = ?
    boolean existsByName(String name);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<Department> searchDepartments(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
