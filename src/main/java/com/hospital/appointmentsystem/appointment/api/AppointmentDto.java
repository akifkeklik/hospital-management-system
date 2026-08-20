package com.hospital.appointmentsystem.appointment.api;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

/**
 * 📦 Appointment DTO — Randevu veri transfer objesi.
 */
public class AppointmentDto {

    private Long id;
    
    @NotNull(message = "Hasta ID boş olamaz")
    private Long patientId;
    
    private String patientFullName;   // Hasta adı soyadı
    
    @NotNull(message = "Doktor ID boş olamaz")
    private Long doctorId;
    
    private String doctorFullName;    // Doktor adı soyadı
    private String departmentName;    // Doktorun bölümü
    
    @NotNull(message = "Randevu tarihi boş olamaz")
    @Future(message = "Randevu tarihi geçmişte olamaz")
    private LocalDateTime appointmentDate;
    
    private String status;            // Enum String olarak
    private String notes;

    public AppointmentDto() {
    }

    // ── Getter ve Setter ──

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientFullName() {
        return patientFullName;
    }

    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorFullName() {
        return doctorFullName;
    }

    public void setDoctorFullName(String doctorFullName) {
        this.doctorFullName = doctorFullName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
