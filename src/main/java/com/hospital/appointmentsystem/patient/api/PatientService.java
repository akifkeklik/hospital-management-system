package com.hospital.appointmentsystem.patient.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 📋 Patient Service Interface — Hasta iş mantığı sözleşmesi.
 */
public interface PatientService {

    PatientDto createPatient(PatientDto patientDto);

    PatientDto getPatientById(Long id);

    Page<PatientDto> getAllPatients(Pageable pageable);

    PatientDto updatePatient(Long id, PatientDto patientDto);

    void deletePatient(Long id);

    Page<PatientDto> searchPatients(String query, Pageable pageable);
}
