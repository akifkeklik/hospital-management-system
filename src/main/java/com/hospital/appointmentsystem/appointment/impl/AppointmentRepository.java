package com.hospital.appointmentsystem.appointment.impl;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

/**
 * 🗄️ Appointment Repository — Randevu veritabanı işlemleri.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.department"})
    Page<Appointment> findAll(Specification<Appointment> spec, Pageable pageable);

    // Hastanın tüm randevularını getir
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.department"})
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    // Doktorun tüm randevularını getir
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.department"})
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    // Belirli durumdaki randevuları getir (ör: tüm SCHEDULED olanlar)
    List<Appointment> findByStatus(AppointmentStatus status);

    // Belli bir tarih aralığındaki doktor randevularını getir
    List<Appointment> findByDoctorIdAndStatusAndAppointmentDateBetween(Long doctorId, AppointmentStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
