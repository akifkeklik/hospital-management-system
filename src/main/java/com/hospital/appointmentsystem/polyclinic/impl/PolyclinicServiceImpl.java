package com.hospital.appointmentsystem.polyclinic.impl;

import com.hospital.appointmentsystem.polyclinic.api.PolyclinicDto;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolyclinicServiceImpl implements PolyclinicService {

    private final PolyclinicRepository repository;

    public PolyclinicServiceImpl(PolyclinicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PolyclinicDto> getAllPolyclinics() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<PolyclinicDto> getPolyclinicsByDepartment(Long departmentId) {
        return repository.findByDepartmentId(departmentId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public PolyclinicDto createPolyclinic(PolyclinicDto dto) {
        Polyclinic polyclinic = new Polyclinic(dto.getName(), dto.getRoomNumber(), dto.getDepartmentId());
        polyclinic = repository.save(polyclinic);
        return mapToDto(polyclinic);
    }

    @Override
    public PolyclinicDto updatePolyclinic(Long id, PolyclinicDto dto) {
        Polyclinic polyclinic = repository.findById(id).orElseThrow(() -> new RuntimeException("Poliklinik bulunamadı"));
        polyclinic.setName(dto.getName());
        polyclinic.setRoomNumber(dto.getRoomNumber());
        polyclinic.setDepartmentId(dto.getDepartmentId());
        polyclinic = repository.save(polyclinic);
        return mapToDto(polyclinic);
    }

    @Override
    public void deletePolyclinic(Long id) {
        repository.deleteById(id);
    }

    private PolyclinicDto mapToDto(Polyclinic polyclinic) {
        return new PolyclinicDto(polyclinic.getId(), polyclinic.getName(), polyclinic.getRoomNumber(), polyclinic.getDepartmentId());
    }
}
