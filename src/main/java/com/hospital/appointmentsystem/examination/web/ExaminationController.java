package com.hospital.appointmentsystem.examination.web;

import com.hospital.appointmentsystem.examination.api.DiagnosisDto;
import com.hospital.appointmentsystem.examination.api.ExaminationService;
import com.hospital.appointmentsystem.examination.api.PrescriptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    private final ExaminationService examinationService;

    public ExaminationController(ExaminationService examinationService) {
        this.examinationService = examinationService;
    }

    // ── TEŞHİS (DIAGNOSIS) ──
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfAppointment(#dto.appointmentId)")
    @PostMapping("/diagnoses")
    public ResponseEntity<DiagnosisDto> addDiagnosis(@RequestBody DiagnosisDto dto) {
        return ResponseEntity.ok(examinationService.addDiagnosis(dto));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfAppointment(#appointmentId) or @securityService.isPatientOfAppointment(#appointmentId)")
    @GetMapping("/appointments/{appointmentId}/diagnoses")
    public ResponseEntity<List<DiagnosisDto>> getDiagnosesByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(examinationService.getDiagnosesByAppointment(appointmentId));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfDiagnosis(#id)")
    @DeleteMapping("/diagnoses/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id) {
        examinationService.deleteDiagnosis(id);
        return ResponseEntity.ok().build();
    }

    // ── REÇETE (PRESCRIPTION) ──
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfAppointment(#dto.appointmentId)")
    @PostMapping("/prescriptions")
    public ResponseEntity<PrescriptionDto> addPrescription(@RequestBody PrescriptionDto dto) {
        return ResponseEntity.ok(examinationService.addPrescription(dto));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfAppointment(#appointmentId) or @securityService.isPatientOfAppointment(#appointmentId)")
    @GetMapping("/appointments/{appointmentId}/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(examinationService.getPrescriptionsByAppointment(appointmentId));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorOfPrescription(#id)")
    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        examinationService.deletePrescription(id);
        return ResponseEntity.ok().build();
    }
}
