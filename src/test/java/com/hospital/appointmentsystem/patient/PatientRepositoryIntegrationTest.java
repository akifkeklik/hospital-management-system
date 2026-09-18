package com.hospital.appointmentsystem.patient;

import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PatientRepositoryIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll(); // Veritabanı izolasyonu

        Patient p1 = new Patient();
        p1.setTcIdentityNumber("11111111111");
        p1.setFirstName("Ahmet");
        p1.setLastName("Yılmaz");
        p1.setPhoneNumber("5551111111");
        p1.setEmail("ahmet@mail.com");
        patientRepository.save(p1);

        Patient p2 = new Patient();
        p2.setTcIdentityNumber("22222222222");
        p2.setFirstName("Mehmet");
        p2.setLastName("Kaya");
        p2.setPhoneNumber("5552222222");
        p2.setEmail("mehmet@mail.com");
        patientRepository.save(p2);

        Patient p3 = new Patient();
        p3.setTcIdentityNumber("33333333333");
        p3.setFirstName("Ayşe");
        p3.setLastName("Demir");
        p3.setPhoneNumber("5553333333");
        p3.setEmail("ayse@mail.com");
        patientRepository.save(p3);
    }

    @Test
    void searchPatients_ScenarioA_ShouldFindByName() {
        Page<Patient> result = patientRepository.searchPatients("ahmet", PageRequest.of(0, 10));
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Ahmet", result.getContent().get(0).getFirstName());
    }

    @Test
    void searchPatients_ScenarioB_ShouldBeCaseInsensitive() {
        Page<Patient> result1 = patientRepository.searchPatients("AHMET", PageRequest.of(0, 10));
        Page<Patient> result2 = patientRepository.searchPatients("ahmet", PageRequest.of(0, 10));
        Page<Patient> result3 = patientRepository.searchPatients("AhMeT", PageRequest.of(0, 10));

        assertEquals(1, result1.getTotalElements());
        assertEquals(1, result2.getTotalElements());
        assertEquals(1, result3.getTotalElements());
        assertEquals(result1.getContent().get(0).getId(), result3.getContent().get(0).getId());
    }

    @Test
    void searchPatients_ScenarioC_ShouldSearchFullName() {
        Page<Patient> result = patientRepository.searchPatients("ahmet yılmaz", PageRequest.of(0, 10));
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Ahmet", result.getContent().get(0).getFirstName());
        assertEquals("Yılmaz", result.getContent().get(0).getLastName());
    }

    @Test
    void searchPatients_ScenarioD_ShouldSearchByTcIdentityNumberPart() {
        Page<Patient> result = patientRepository.searchPatients("222222", PageRequest.of(0, 10));
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Mehmet", result.getContent().get(0).getFirstName());
    }

    @Test
    void searchPatients_ScenarioE_EmptyResult() {
        Page<Patient> result = patientRepository.searchPatients("olmayan_kisi", PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void searchPatients_ScenarioF_Pagination() {
        // Toplam 3 kayıt var
        // page = 0, size = 1
        Pageable pageable1 = PageRequest.of(0, 1);
        Page<Patient> result1 = patientRepository.searchPatients("a", pageable1); // "Ahmet" ve "Ayşe" ve "Mehmet" 'Kaya' -> "a" harfi hepsinde var mı?
        // Wait, "a" is in "Ahmet Yılmaz", "Mehmet Kaya", "Ayşe Demir". 
        // "ahmet", "kaya", "ayşe" ... 
        // Let's use empty string or a common letter like "e" (Ahmet, Mehmet, Ayşe) -> all 3 have 'e'
        Pageable pageable2 = PageRequest.of(1, 1);
        
        Page<Patient> resultE1 = patientRepository.searchPatients("e", pageable1);
        assertEquals(3, resultE1.getTotalElements());
        assertEquals(3, resultE1.getTotalPages());
        assertEquals(1, resultE1.getContent().size());

        Page<Patient> resultE2 = patientRepository.searchPatients("e", pageable2);
        assertEquals(1, resultE2.getContent().size());
        assertNotEquals(resultE1.getContent().get(0).getId(), resultE2.getContent().get(0).getId());
    }
}
