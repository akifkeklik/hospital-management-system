package com.hospital.appointmentsystem.examination.api;

import java.util.List;

public interface ExaminationService {
    DiagnosisDto addDiagnosis(DiagnosisDto diagnosisDto);
    List<DiagnosisDto> getDiagnosesByAppointment(Long appointmentId);
    void deleteDiagnosis(Long id);

    PrescriptionDto addPrescription(PrescriptionDto prescriptionDto);
    List<PrescriptionDto> getPrescriptionsByAppointment(Long appointmentId);
    void deletePrescription(Long id);
}
