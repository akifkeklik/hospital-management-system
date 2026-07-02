package com.hospital.appointmentsystem.polyclinic.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolyclinicRepository extends JpaRepository<Polyclinic, Long> {
    List<Polyclinic> findByDepartmentId(Long departmentId);
}
