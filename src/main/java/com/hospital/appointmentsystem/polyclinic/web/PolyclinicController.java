package com.hospital.appointmentsystem.polyclinic.web;

import com.hospital.appointmentsystem.polyclinic.api.PolyclinicDto;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/polyclinics")
public class PolyclinicController {

    private final PolyclinicService polyclinicService;

    public PolyclinicController(PolyclinicService polyclinicService) {
        this.polyclinicService = polyclinicService;
    }

    @GetMapping
    public ResponseEntity<List<PolyclinicDto>> getAllPolyclinics() {
        return ResponseEntity.ok(polyclinicService.getAllPolyclinics());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<PolyclinicDto>> getPolyclinicsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(polyclinicService.getPolyclinicsByDepartment(departmentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolyclinicDto> createPolyclinic(@RequestBody PolyclinicDto dto) {
        return ResponseEntity.ok(polyclinicService.createPolyclinic(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolyclinicDto> updatePolyclinic(@PathVariable Long id, @RequestBody PolyclinicDto dto) {
        return ResponseEntity.ok(polyclinicService.updatePolyclinic(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePolyclinic(@PathVariable Long id) {
        polyclinicService.deletePolyclinic(id);
        return ResponseEntity.ok().build();
    }
}
