package com.hospital.appointmentsystem.doctorleave.impl;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_leaves", indexes = {
    @Index(name = "IDX_DOCTOR_LEAVE_DOC_DATES", columnList = "doctor_id, start_date, end_date")
})
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String reason; // Yıllık İzin, Hastalık vb.

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    public DoctorLeave() {
    }

    public DoctorLeave(Long doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        this.doctorId = doctorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
