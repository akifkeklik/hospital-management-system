package com.hospital.appointmentsystem.doctor.impl;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_registration_requests")
public class DoctorRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tc_identity_number", nullable = false, length = 11)
    private String tcIdentityNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    public DoctorRegistrationRequest() {
    }

    @PrePersist
    protected void onCreate() {
        this.requestDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTcIdentityNumber() { return tcIdentityNumber; }
    public void setTcIdentityNumber(String tcIdentityNumber) { this.tcIdentityNumber = tcIdentityNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
}
