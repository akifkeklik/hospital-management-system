package com.hospital.appointmentsystem.doctorleave.api;

import java.time.LocalDate;

public class DoctorLeaveDto {
    private Long id;
    private Long doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;

    public DoctorLeaveDto() {}

    public DoctorLeaveDto(Long id, Long doctorId, LocalDate startDate, LocalDate endDate, String reason, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
