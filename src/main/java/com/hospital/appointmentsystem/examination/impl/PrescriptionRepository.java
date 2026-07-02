package com.hospital.appointmentsystem.examination.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByAppointmentId(Long appointmentId);
}
