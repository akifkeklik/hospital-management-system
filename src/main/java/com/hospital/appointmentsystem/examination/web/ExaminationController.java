package com.hospital.appointmentsystem.examination.web;

import com.hospital.appointmentsystem.examination.api.DiagnosisDto;
import com.hospital.appointmentsystem.examination.api.ExaminationService;
import com.hospital.appointmentsystem.examination.api.PrescriptionDto;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/diagnoses")
    public ResponseEntity<DiagnosisDto> addDiagnosis(@RequestBody DiagnosisDto dto) {
        return ResponseEntity.ok(examinationService.addDiagnosis(dto));
    }

    @GetMapping("/appointments/{appointmentId}/diagnoses")
    public ResponseEntity<List<DiagnosisDto>> getDiagnosesByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(examinationService.getDiagnosesByAppointment(appointmentId));
    }

    @DeleteMapping("/diagnoses/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id) {
        examinationService.deleteDiagnosis(id);
        return ResponseEntity.ok().build();
    }

    // ── REÇETE (PRESCRIPTION) ──
    @PostMapping("/prescriptions")
    public ResponseEntity<PrescriptionDto> addPrescription(@RequestBody PrescriptionDto dto) {
        return ResponseEntity.ok(examinationService.addPrescription(dto));
    }

    @GetMapping("/appointments/{appointmentId}/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptionsByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(examinationService.getPrescriptionsByAppointment(appointmentId));
    }

    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        examinationService.deletePrescription(id);
        return ResponseEntity.ok().build();
    }
}
