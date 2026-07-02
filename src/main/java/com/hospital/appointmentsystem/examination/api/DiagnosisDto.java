package com.hospital.appointmentsystem.examination.api;

public class DiagnosisDto {
    private Long id;
    private Long appointmentId;
    private String icd10Code;
    private String description;

    public DiagnosisDto() {}

    public DiagnosisDto(Long id, Long appointmentId, String icd10Code, String description) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.icd10Code = icd10Code;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getIcd10Code() { return icd10Code; }
    public void setIcd10Code(String icd10Code) { this.icd10Code = icd10Code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
