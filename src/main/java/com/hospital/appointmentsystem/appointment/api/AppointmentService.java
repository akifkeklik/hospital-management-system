package com.hospital.appointmentsystem.appointment.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;

/**
 * 📋 Appointment Service Interface — Randevu iş mantığı sözleşmesi.
 */
public interface AppointmentService {

    AppointmentDto createAppointment(AppointmentDto appointmentDto);

    AppointmentDto getAppointmentById(Long id);

    Page<AppointmentDto> getAllAppointments(Pageable pageable);

    /** Hastanın tüm randevularını getir */
    Page<AppointmentDto> getAppointmentsByPatientId(Long patientId, List<AppointmentStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /** Doktorun tüm randevularını getir */
    Page<AppointmentDto> getAppointmentsByDoctorId(Long doctorId, List<AppointmentStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /** Belirli bir tarih ve doktor için müsait (boş) saat dilimlerini getir */
    List<String> getAvailableSlots(Long doctorId, java.time.LocalDate date);

    /** Randevuyu iptal et */
    AppointmentDto cancelAppointment(Long id);

    AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto);

    /** Randevu durumunu güncelle (ör: SCHEDULED → COMPLETED) */
    AppointmentDto updateAppointmentStatus(Long id, String status);

    void deleteAppointment(Long id);

    /** Belirli bir randevu için tahmini bekleme süresi ve yoğunluk bilgisini getir */
    WaitTimeDto getEstimatedWaitTime(Long appointmentId);
}
