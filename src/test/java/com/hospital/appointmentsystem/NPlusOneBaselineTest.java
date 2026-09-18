package com.hospital.appointmentsystem;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.hospital.appointmentsystem.HibernateQueryInterceptor"
})
public class NPlusOneBaselineTest {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @Transactional
    public void testPatientAppointmentsBaseline() {
        System.out.println("=== PATIENT APPOINTMENTS BASELINE START ===");
        
        // Setup: Create 5 appointments for patient 1 with 5 different doctors
        Patient patient = patientRepository.findById(1L).orElseThrow();
        for (long i = 1; i <= 5; i++) {
            Doctor doc = doctorRepository.findById(i).orElse(null);
            if (doc != null) {
                Appointment a = new Appointment();
                a.setPatient(patient);
                a.setDoctor(doc);
                a.setAppointmentDate(LocalDateTime.now().plusDays(i));
                a.setStatus(AppointmentStatus.SCHEDULED);
                a.setNotes("Baseline Test");
                appointmentRepository.save(a);
            }
        }
        appointmentRepository.flush(); // ensure inserts are done
        
        // CLEAR FIRST LEVEL CACHE TO SIMULATE A FRESH REQUEST
        entityManager.clear();

        HibernateQueryInterceptor.startQueryCount();
        
        // Fetch appointments for patient 1 (Should trigger N+1)
        Page<AppointmentDto> page = appointmentService.getAppointmentsByPatientId(1L, null, null, null, PageRequest.of(0, 100));
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed (Patient): " + queryCount);
        
        assertNotNull(page);
        assertTrue(queryCount <= 2, "Patient appointments query count should be <= 2, was: " + queryCount);
        System.out.println("=== PATIENT APPOINTMENTS BASELINE END ===");
    }
    
    @Test
    public void testDoctorAppointmentsBaseline() {
        System.out.println("=== DOCTOR APPOINTMENTS BASELINE START ===");
        HibernateQueryInterceptor.startQueryCount();
        
        Page<AppointmentDto> page = appointmentService.getAppointmentsByDoctorId(1L, null, null, null, PageRequest.of(0, 100));
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed: " + queryCount);
        assertNotNull(page);
        assertTrue(queryCount <= 2, "Doctor appointments query count should be <= 2, was: " + queryCount);
        System.out.println("=== DOCTOR APPOINTMENTS BASELINE END ===");
    }
    
    @Test
    public void testDepartmentDoctorsBaseline() {
        System.out.println("=== DEPARTMENT DOCTORS BASELINE START ===");
        HibernateQueryInterceptor.startQueryCount();
        
        var list = doctorService.getDoctorsByDepartmentId(1L);
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed: " + queryCount);
        assertNotNull(list);
        assertTrue(queryCount <= 2, "Department doctors query count should be <= 2, was: " + queryCount);
        System.out.println("=== DEPARTMENT DOCTORS BASELINE END ===");
    }

    @Test
    @Transactional
    @DisplayName("N+1 Regression Guard: DoctorService.getAllDoctors must execute at most 2 queries with joins")
    public void testGetAllDoctorsNPlusOnePrevention() {
        entityManager.flush();
        entityManager.clear();

        HibernateQueryInterceptor.startQueryCount();
        Page<DoctorDto> doctors = doctorService.getAllDoctors(PageRequest.of(0, 20));

        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("=== GET ALL DOCTORS QUERY COUNT: " + queryCount + " (Rows: " + doctors.getNumberOfElements() + ") ===");

        assertNotNull(doctors);
        assertTrue(doctors.getNumberOfElements() >= 5,
            "Expected multiple doctor rows (>= 5) to validate N+1 prevention, but found: " + doctors.getNumberOfElements());

        // Verify lazy relationship data is populated in DTO without triggering extra queries
        for (DoctorDto doc : doctors) {
            assertNotNull(doc.getDepartmentName(), "Department name must be populated");
            assertNotNull(doc.getPolyclinicName(), "Polyclinic name must be populated");
        }

        // Regression Guard:
        // Without @EntityGraph: 1 + 2N queries were executed (25 queries for 16 rows).
        // With @EntityGraph: 1 query (or at most 2 with count query).
        assertTrue(queryCount <= 2,
            "N+1 regression detected! DoctorService.getAllDoctors executed " + queryCount + " queries, expected <= 2");
    }

    @Test
    @Transactional
    @DisplayName("N+1 Regression Guard: AppointmentService.getAllAppointments must execute at most 2 queries with joins")
    public void testGetAllAppointmentsNPlusOnePrevention() {
        entityManager.flush();
        entityManager.clear();

        HibernateQueryInterceptor.startQueryCount();
        Page<AppointmentDto> appointments = appointmentService.getAllAppointments(PageRequest.of(0, 20));

        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("=== GET ALL APPOINTMENTS QUERY COUNT: " + queryCount + " (Rows: " + appointments.getNumberOfElements() + ") ===");

        assertNotNull(appointments);
        assertTrue(appointments.getNumberOfElements() >= 5,
            "Expected multiple appointment rows (>= 5) to validate N+1 prevention, but found: " + appointments.getNumberOfElements());

        // Verify lazy relationship data is populated in DTO without triggering extra queries
        for (AppointmentDto appt : appointments) {
            assertNotNull(appt.getPatientFullName(), "Patient full name must be populated");
            assertNotNull(appt.getDoctorFullName(), "Doctor full name must be populated");
            assertNotNull(appt.getDepartmentName(), "Department name must be populated");
        }

        // Regression Guard:
        // Without @EntityGraph: 1 + 3N queries were executed (16 queries for 6 rows).
        // With @EntityGraph: 1 query (or at most 2 with count query).
        assertTrue(queryCount <= 2,
            "N+1 regression detected! AppointmentService.getAllAppointments executed " + queryCount + " queries, expected <= 2");
    }
}
