package com.hospital.appointmentsystem.examination.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findByAppointmentId(Long appointmentId);
}
