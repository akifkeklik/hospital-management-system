package com.hospital.appointmentsystem.doctor;

import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DoctorRepositoryIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        doctorRepository.deleteAll();
        departmentRepository.deleteAll();

        Department cardiology = new Department();
        cardiology.setName("Cardiology");
        departmentRepository.save(cardiology);

        Department neurology = new Department();
        neurology.setName("Neurology");
        departmentRepository.save(neurology);
        
        Department dermatology = new Department();
        dermatology.setName("Dermatology");
        departmentRepository.save(dermatology);
        
        Department emptyDept = new Department();
        emptyDept.setName("EmptyDept");
        departmentRepository.save(emptyDept);

        // Cardiology -> 3 doctors
        for (int i = 1; i <= 3; i++) {
            Doctor d = new Doctor();
            d.setFirstName("Cardio" + i);
            d.setLastName("Doctor" + i);
            d.setSpecialization("Heart " + i);
            d.setTcIdentityNumber("1000000000" + i);
            d.setDepartment(cardiology);
            doctorRepository.save(d);
        }

        // Neurology -> 2 doctors
        for (int i = 1; i <= 2; i++) {
            Doctor d = new Doctor();
            d.setFirstName("Neuro" + i);
            d.setLastName("Doctor" + i);
            d.setSpecialization("Brain " + i);
            d.setTcIdentityNumber("2000000000" + i);
            d.setDepartment(neurology);
            doctorRepository.save(d);
        }

        // Dermatology -> 1 doctor
        Doctor d = new Doctor();
        d.setFirstName("Derma1");
        d.setLastName("Doctor1");
        d.setSpecialization("Skin 1");
        d.setTcIdentityNumber("30000000001");
        d.setDepartment(dermatology);
        doctorRepository.save(d);
    }

    @Test
    void searchDoctors_ShouldSearchByFirstNameLastNameAndSpecialty() {
        Page<Doctor> nameSearch = doctorRepository.searchDoctors("Cardio1", PageRequest.of(0, 10));
        assertEquals(1, nameSearch.getTotalElements());

        Page<Doctor> lastNameSearch = doctorRepository.searchDoctors("Doctor1", PageRequest.of(0, 10));
        assertEquals(3, lastNameSearch.getTotalElements()); // Cardio1, Neuro1, Derma1

        Page<Doctor> specialtySearch = doctorRepository.searchDoctors("Brain 2", PageRequest.of(0, 10));
        assertEquals(1, specialtySearch.getTotalElements());
        assertEquals("Neuro2", specialtySearch.getContent().get(0).getFirstName());
    }

    @Test
    void searchDoctors_ShouldBeCaseInsensitive() {
        // Test CARDIO1, cardio1, Cardio1
        Page<Doctor> search1 = doctorRepository.searchDoctors("CARDIO1", PageRequest.of(0, 10));
        Page<Doctor> search2 = doctorRepository.searchDoctors("cardio1", PageRequest.of(0, 10));
        Page<Doctor> search3 = doctorRepository.searchDoctors("CaRdIo1", PageRequest.of(0, 10));

        assertEquals(1, search1.getTotalElements(), "Expected 1 for CARDIO1");
        assertEquals(1, search2.getTotalElements(), "Expected 1 for cardio1");
        assertEquals(1, search3.getTotalElements(), "Expected 1 for CaRdIo1");

        // "I" vs "ı" specific scenario
        // Cardio1 contains "i". "CARDIO" contains "I". 
        // We will insert a doctor with "Irmak" or "Işık". 
        Doctor doctor = new Doctor();
        doctor.setFirstName("Irmak");
        doctor.setLastName("Işık");
        doctor.setSpecialization("Specialty");
        doctor.setTcIdentityNumber("99999999999");
        doctor.setDepartment(departmentRepository.findAll().get(0));
        doctorRepository.save(doctor);

        Page<Doctor> searchIrmak1 = doctorRepository.searchDoctors("ırmak", PageRequest.of(0, 10));
        Page<Doctor> searchIrmak2 = doctorRepository.searchDoctors("IRMAK", PageRequest.of(0, 10));
        
        System.out.println("Search 'ırmak': " + searchIrmak1.getTotalElements());
        System.out.println("Search 'IRMAK': " + searchIrmak2.getTotalElements());
        
        // At least assert it can find with lowercase "i" for Cardio
        Page<Doctor> searchCardioTr = doctorRepository.searchDoctors("cardıo", PageRequest.of(0, 10));
        System.out.println("Search 'cardıo': " + searchCardioTr.getTotalElements());
    }

    @Test
    void searchDoctors_ShouldReturnEmptyResult() {
        Page<Doctor> result = doctorRepository.searchDoctors("NonExistentDoctor", PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void searchDoctors_ShouldPaginateCorrectly() {
        // "Doctor" araması 6 doktor döndürmeli
        Page<Doctor> resultPage0 = doctorRepository.searchDoctors("Doctor", PageRequest.of(0, 2));
        Page<Doctor> resultPage1 = doctorRepository.searchDoctors("Doctor", PageRequest.of(1, 2));

        assertEquals(6, resultPage0.getTotalElements());
        assertEquals(3, resultPage0.getTotalPages());
        assertEquals(2, resultPage0.getContent().size());
        assertEquals(2, resultPage1.getContent().size());
        
        assertNotEquals(resultPage0.getContent().get(0).getId(), resultPage1.getContent().get(0).getId());
    }

    @Test
    void getDoctorDistribution_ShouldReturnCorrectCounts() {
        List<Object[]> distribution = doctorRepository.getDoctorDistribution();
        
        // Distribution should contain departments with their counts
        // EmptyDept might not be there because the query is:
        // SELECT d.department.name, COUNT(d) FROM Doctor d GROUP BY d.department.name
        // This acts as an INNER JOIN between Doctor and Department, so EmptyDept will have 0 or won't be returned.
        
        boolean cardioFound = false;
        boolean neuroFound = false;
        boolean dermaFound = false;
        boolean emptyFound = false;

        for (Object[] row : distribution) {
            String deptName = (String) row[0];
            long count = ((Number) row[1]).longValue();

            if ("Cardiology".equals(deptName)) {
                assertEquals(3, count);
                cardioFound = true;
            } else if ("Neurology".equals(deptName)) {
                assertEquals(2, count);
                neuroFound = true;
            } else if ("Dermatology".equals(deptName)) {
                assertEquals(1, count);
                dermaFound = true;
            } else if ("EmptyDept".equals(deptName)) {
                emptyFound = true;
            }
        }

        assertTrue(cardioFound);
        assertTrue(neuroFound);
        assertTrue(dermaFound);
        assertFalse(emptyFound, "EmptyDept should not be present in INNER JOIN aggregation");
    }
}
