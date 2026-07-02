package com.hospital.appointmentsystem.examination.api;

public class PrescriptionDto {
    private Long id;
    private Long appointmentId;
    private String medicationName;
    private String dosage;
    private String usageInstruction;

    public PrescriptionDto() {}

    public PrescriptionDto(Long id, Long appointmentId, String medicationName, String dosage, String usageInstruction) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.usageInstruction = usageInstruction;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getUsageInstruction() { return usageInstruction; }
    public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }
}
