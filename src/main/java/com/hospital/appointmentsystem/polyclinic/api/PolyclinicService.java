package com.hospital.appointmentsystem.polyclinic.api;

import java.util.List;

public interface PolyclinicService {
    List<PolyclinicDto> getAllPolyclinics();
    List<PolyclinicDto> getPolyclinicsByDepartment(Long departmentId);
    PolyclinicDto createPolyclinic(PolyclinicDto dto);
    PolyclinicDto updatePolyclinic(Long id, PolyclinicDto dto);
    void deletePolyclinic(Long id);
}
