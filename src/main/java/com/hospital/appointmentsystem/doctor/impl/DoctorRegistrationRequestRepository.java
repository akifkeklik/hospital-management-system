package com.hospital.appointmentsystem.doctor.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRegistrationRequestRepository extends JpaRepository<DoctorRegistrationRequest, Long> {
    List<DoctorRegistrationRequest> findByStatusOrderByRequestDateDesc(DoctorRegistrationStatus status);
}
