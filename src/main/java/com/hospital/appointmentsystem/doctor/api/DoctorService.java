package com.hospital.appointmentsystem.doctor.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 📋 Doctor Service Interface — Doktor iş mantığı sözleşmesi.
 */
public interface DoctorService {

    DoctorDto createDoctor(DoctorDto doctorDto);

    DoctorDto getDoctorById(Long id);

    Page<DoctorDto> getAllDoctors(Pageable pageable);

    /**
     * Belirli bir bölümdeki doktorları getirir.
     * Örneğin: Kardiyoloji bölümündeki tüm doktorlar
     */
    List<DoctorDto> getDoctorsByDepartmentId(Long departmentId);

    DoctorDto updateDoctor(Long id, DoctorDto doctorDto);

    void deleteDoctor(Long id);

    Page<DoctorDto> searchDoctors(String query, Pageable pageable);
}
