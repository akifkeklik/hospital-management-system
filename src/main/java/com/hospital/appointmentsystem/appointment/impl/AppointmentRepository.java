package com.hospital.appointmentsystem.appointment.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 🗄️ Appointment Repository — Randevu veritabanı işlemleri.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Hastanın tüm randevularını getir
    List<Appointment> findByPatientId(Long patientId);

    // Doktorun tüm randevularını getir
    List<Appointment> findByDoctorId(Long doctorId);

    // Belirli durumdaki randevuları getir (ör: tüm SCHEDULED olanlar)
    List<Appointment> findByStatus(AppointmentStatus status);

    // Belli bir tarih aralığındaki doktor randevularını getir
    List<Appointment> findByDoctorIdAndStatusAndAppointmentDateBetween(Long doctorId, AppointmentStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
