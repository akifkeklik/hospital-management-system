package com.hospital.appointmentsystem.polyclinic.impl;

import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicDto;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class PolyclinicServiceImpl implements PolyclinicService {

    private final PolyclinicRepository repository;

    public PolyclinicServiceImpl(PolyclinicRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "polyclinics")
    public List<PolyclinicDto> getAllPolyclinics() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "polyclinics", key = "#departmentId")
    public List<PolyclinicDto> getPolyclinicsByDepartment(Long departmentId) {
        return repository.findByDepartmentId(departmentId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "polyclinics", allEntries = true)
    public PolyclinicDto createPolyclinic(PolyclinicDto polyclinicDto) {
        Polyclinic polyclinic = new Polyclinic(polyclinicDto.getName(), polyclinicDto.getRoomNumber(), polyclinicDto.getDepartmentId());
        polyclinic = repository.save(polyclinic);
        return mapToDto(polyclinic);
    }

    @Override
    @CacheEvict(value = "polyclinics", allEntries = true)
    public PolyclinicDto updatePolyclinic(Long id, PolyclinicDto polyclinicDto) {
        Polyclinic polyclinic = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Polyclinic", "id", id));
        polyclinic.setName(polyclinicDto.getName());
        polyclinic.setRoomNumber(polyclinicDto.getRoomNumber());
        polyclinic.setDepartmentId(polyclinicDto.getDepartmentId());
        polyclinic = repository.save(polyclinic);
        return mapToDto(polyclinic);
    }

    @Override
    @CacheEvict(value = "polyclinics", allEntries = true)
    public void deletePolyclinic(Long id) {
        repository.deleteById(id);
    }

    private PolyclinicDto mapToDto(Polyclinic polyclinic) {
        return new PolyclinicDto(polyclinic.getId(), polyclinic.getName(), polyclinic.getRoomNumber(), polyclinic.getDepartmentId());
    }
}
