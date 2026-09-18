package com.hospital.appointmentsystem.appointment;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentSpecification;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AppointmentSpecificationIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Patient patient1, patient2;
    private Doctor doctor1, doctor2;
    private Department testDept;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        departmentRepository.deleteAll();

        testDept = new Department();
        testDept.setName("Test Dept");
        testDept = departmentRepository.save(testDept);

        patient1 = createPatient("11111111111", "Patient", "One");
        patient2 = createPatient("22222222222", "Patient", "Two");

        doctor1 = createDoctor("10000000001", "Doctor", "One");
        doctor2 = createDoctor("20000000002", "Doctor", "Two");

        baseTime = LocalDateTime.of(2025, 1, 1, 10, 0);

        // Appt 1: patient1, doctor1, SCHEDULED, baseTime
        appointmentRepository.save(createAppt(doctor1, patient1, baseTime, AppointmentStatus.SCHEDULED));
        // Appt 2: patient1, doctor2, COMPLETED, baseTime + 1 day
        appointmentRepository.save(createAppt(doctor2, patient1, baseTime.plusDays(1), AppointmentStatus.COMPLETED));
        // Appt 3: patient2, doctor1, CANCELLED, baseTime + 2 days
        appointmentRepository.save(createAppt(doctor1, patient2, baseTime.plusDays(2), AppointmentStatus.CANCELLED));
        // Appt 4: patient2, doctor2, SCHEDULED, baseTime + 3 days
        appointmentRepository.save(createAppt(doctor2, patient2, baseTime.plusDays(3), AppointmentStatus.SCHEDULED));
    }

    private Patient createPatient(String tc, String first, String last) {
        Patient p = new Patient();
        p.setTcIdentityNumber(tc);
        p.setFirstName(first);
        p.setLastName(last);
        p.setPhoneNumber("5550000000");
        p.setEmail(first + "@mail.com");
        return patientRepository.save(p);
    }

    private Doctor createDoctor(String tc, String first, String last) {
        Doctor d = new Doctor();
        d.setTcIdentityNumber(tc);
        d.setFirstName(first);
        d.setLastName(last);
        d.setSpecialization("Spec");
        d.setDepartment(testDept);
        return doctorRepository.save(d);
    }

    private Appointment createAppt(Doctor d, Patient p, LocalDateTime date, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setDoctor(d);
        a.setPatient(p);
        a.setAppointmentDate(date);
        a.setStatus(status);
        a.setNotes("Test Notes");
        return a;
    }

    @Test
    void filterBy_PatientId() {
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(patient1.getId(), null, null, null, null),
                PageRequest.of(0, 10)
        );
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(a -> a.getPatient().getId().equals(patient1.getId())));
    }

    @Test
    void filterBy_DoctorId() {
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, doctor2.getId(), null, null, null),
                PageRequest.of(0, 10)
        );
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(a -> a.getDoctor().getId().equals(doctor2.getId())));
    }

    @Test
    void filterBy_MultipleStatuses() {
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, Arrays.asList(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED), null, null),
                PageRequest.of(0, 10)
        );
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(a -> 
            a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CANCELLED));
    }

    @Test
    void filterBy_DateRange() {
        LocalDateTime start = baseTime.plusDays(1).with(LocalTime.MIN);
        LocalDateTime end = baseTime.plusDays(2).with(LocalTime.MAX);
        
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, null, start, end),
                PageRequest.of(0, 10)
        );
        
        // Should find Appt 2 and Appt 3
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void filterBy_CombinedFilters() {
        // Patient1, SCHEDULED
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(patient1.getId(), null, Collections.singletonList(AppointmentStatus.SCHEDULED), null, null),
                PageRequest.of(0, 10)
        );
        
        assertEquals(1, result.getTotalElements());
        assertEquals(AppointmentStatus.SCHEDULED, result.getContent().get(0).getStatus());
        assertEquals(patient1.getId(), result.getContent().get(0).getPatient().getId());
    }

    @Test
    void filterBy_EmptyFilters() {
        Page<Appointment> result = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, null, null, null),
                PageRequest.of(0, 10)
        );
        
        assertEquals(4, result.getTotalElements());
    }

    @Test
    void pagination_BoundaryTests() {
        // Ensure 4 records exist (from setup)
        
        // Page 0, Size 1
        Page<Appointment> page0 = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, null, null, null),
                PageRequest.of(0, 1)
        );
        assertEquals(4, page0.getTotalElements());
        assertEquals(4, page0.getTotalPages());
        assertEquals(1, page0.getContent().size());
        
        // Page 1, Size 1
        Page<Appointment> page1 = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, null, null, null),
                PageRequest.of(1, 1)
        );
        assertEquals(1, page1.getContent().size());
        assertTrue(page0.getContent().get(0).getId() != page1.getContent().get(0).getId());

        // Beyond last page (Page 10, Size 1)
        Page<Appointment> page10 = appointmentRepository.findAll(
                AppointmentSpecification.filterBy(null, null, null, null, null),
                PageRequest.of(10, 1)
        );
        assertEquals(4, page10.getTotalElements());
        assertEquals(0, page10.getContent().size());
    }
}
