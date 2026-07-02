package com.hospital.appointmentsystem.examination.impl;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.examination.api.DiagnosisDto;
import com.hospital.appointmentsystem.examination.api.ExaminationService;
import com.hospital.appointmentsystem.examination.api.PrescriptionDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExaminationServiceImpl implements ExaminationService {

    private final DiagnosisRepository diagnosisRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    public ExaminationServiceImpl(DiagnosisRepository diagnosisRepository, 
                                  PrescriptionRepository prescriptionRepository, 
                                  AppointmentRepository appointmentRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public DiagnosisDto addDiagnosis(DiagnosisDto dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));
        Diagnosis diagnosis = new Diagnosis(appointment, dto.getIcd10Code(), dto.getDescription());
        diagnosis = diagnosisRepository.save(diagnosis);
        return mapToDto(diagnosis);
    }

    @Override
    public List<DiagnosisDto> getDiagnosesByAppointment(Long appointmentId) {
        return diagnosisRepository.findByAppointmentId(appointmentId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteDiagnosis(Long id) {
        diagnosisRepository.deleteById(id);
    }

    @Override
    public PrescriptionDto addPrescription(PrescriptionDto dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));
        Prescription prescription = new Prescription(appointment, dto.getMedicationName(), dto.getDosage(), dto.getUsageInstruction());
        prescription = prescriptionRepository.save(prescription);
        return mapToDto(prescription);
    }

    @Override
    public List<PrescriptionDto> getPrescriptionsByAppointment(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deletePrescription(Long id) {
        prescriptionRepository.deleteById(id);
    }

    private DiagnosisDto mapToDto(Diagnosis diagnosis) {
        return new DiagnosisDto(diagnosis.getId(), diagnosis.getAppointment().getId(), diagnosis.getIcd10Code(), diagnosis.getDescription());
    }

    private PrescriptionDto mapToDto(Prescription prescription) {
        return new PrescriptionDto(prescription.getId(), prescription.getAppointment().getId(), prescription.getMedicationName(), prescription.getDosage(), prescription.getUsageInstruction());
    }
}
