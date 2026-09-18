package com.hospital.appointmentsystem.patient;

import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.patient.api.PatientDto;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.patient.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientDto validPatientDto;

    @BeforeEach
    void setUp() {
        validPatientDto = new PatientDto();
        validPatientDto.setTcIdentityNumber("12345678901");
        validPatientDto.setFirstName("Ahmet");
        validPatientDto.setLastName("Yılmaz");
    }

    @Test
    void shouldThrowBusinessRuleException_WhenDuplicateTcIdentityNumberProvided() {
        // Arrange
        when(patientRepository.existsByTcIdentityNumber("12345678901")).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            patientService.createPatient(validPatientDto);
        });

        // Assert message
        assertTrue(exception.getMessage().contains("Bu TC Kimlik No ile kayıtlı hasta zaten var: 12345678901"));

        // Verify that save was never called
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void shouldCreatePatient_WhenTcIdentityNumberIsUnique() {
        // Arrange
        when(patientRepository.existsByTcIdentityNumber("12345678901")).thenReturn(false);
        
        Patient savedPatient = new Patient();
        savedPatient.setId(1L);
        savedPatient.setTcIdentityNumber("12345678901");
        savedPatient.setFirstName("Ahmet");
        savedPatient.setLastName("Yılmaz");
        
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientDto result = patientService.createPatient(validPatientDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345678901", result.getTcIdentityNumber());
        
        verify(patientRepository).existsByTcIdentityNumber("12345678901");
        verify(patientRepository).save(any(Patient.class));
    }
}
