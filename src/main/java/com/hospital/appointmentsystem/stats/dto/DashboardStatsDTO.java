package com.hospital.appointmentsystem.stats.dto;

import java.util.Map;

public class DashboardStatsDTO {
    private long totalDepartments;
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private Map<String, Long> doctorDistribution;
    private Map<String, Long> appointmentsByDate;

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public Map<String, Long> getDoctorDistribution() {
        return doctorDistribution;
    }

    public void setDoctorDistribution(Map<String, Long> doctorDistribution) {
        this.doctorDistribution = doctorDistribution;
    }

    public Map<String, Long> getAppointmentsByDate() {
        return appointmentsByDate;
    }

    public void setAppointmentsByDate(Map<String, Long> appointmentsByDate) {
        this.appointmentsByDate = appointmentsByDate;
    }
}
