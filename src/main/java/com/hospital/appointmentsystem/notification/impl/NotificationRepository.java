package com.hospital.appointmentsystem.notification.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Notification> findByPatientIdAndIsReadFalse(Long patientId);
    List<Notification> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<Notification> findByDoctorIdAndIsReadFalse(Long doctorId);
}
