package com.hospital.appointmentsystem.examination.impl;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import jakarta.persistence.*;

@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "icd10_code", nullable = false, length = 10)
    private String icd10Code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    public Diagnosis() {}

    public Diagnosis(Appointment appointment, String icd10Code, String description) {
        this.appointment = appointment;
        this.icd10Code = icd10Code;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getIcd10Code() { return icd10Code; }
    public void setIcd10Code(String icd10Code) { this.icd10Code = icd10Code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
