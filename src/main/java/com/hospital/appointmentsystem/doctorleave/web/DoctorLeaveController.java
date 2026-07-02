package com.hospital.appointmentsystem.doctorleave.web;

import com.hospital.appointmentsystem.doctorleave.api.DoctorLeaveDto;
import com.hospital.appointmentsystem.doctorleave.api.DoctorLeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-leaves")
public class DoctorLeaveController {

    private final DoctorLeaveService doctorLeaveService;

    public DoctorLeaveController(DoctorLeaveService doctorLeaveService) {
        this.doctorLeaveService = doctorLeaveService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorLeaveDto>> getAllLeaves() {
        return ResponseEntity.ok(doctorLeaveService.getAllLeaves());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorLeaveDto>> getLeavesByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorLeaveService.getLeavesByDoctorId(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DoctorLeaveDto> createLeave(@RequestBody DoctorLeaveDto dto) {
        return ResponseEntity.ok(doctorLeaveService.createLeave(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorLeaveDto> updateLeaveStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        return ResponseEntity.ok(doctorLeaveService.updateLeaveStatus(id, newStatus));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        doctorLeaveService.deleteLeave(id);
        return ResponseEntity.ok().build();
    }
}
