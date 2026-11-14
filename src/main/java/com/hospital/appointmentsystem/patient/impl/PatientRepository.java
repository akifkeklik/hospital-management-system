package com.hospital.appointmentsystem.patient.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // TC Kimlik No ile hasta bul
    // Optional döner → hasta bulunamayabilir
    Optional<Patient> findByTcIdentityNumber(String tcIdentityNumber);

    // Bu TC ile hasta var mı?
    boolean existsByTcIdentityNumber(String tcIdentityNumber);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE CONCAT(p.firstName, ' ', p.lastName) ILIKE CONCAT('%', :query, '%') OR p.tcIdentityNumber ILIKE CONCAT('%', :query, '%')")
    org.springframework.data.domain.Page<Patient> searchPatients(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
