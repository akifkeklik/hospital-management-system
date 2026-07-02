package com.hospital.appointmentsystem.examination.impl;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import jakarta.persistence.*;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "medication_name", nullable = false, length = 100)
    private String medicationName;

    @Column(name = "dosage", nullable = false, length = 50)
    private String dosage;

    @Column(name = "usage_instruction", nullable = false, length = 255)
    private String usageInstruction;

    public Prescription() {}

    public Prescription(Appointment appointment, String medicationName, String dosage, String usageInstruction) {
        this.appointment = appointment;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.usageInstruction = usageInstruction;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getUsageInstruction() { return usageInstruction; }
    public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }
}
