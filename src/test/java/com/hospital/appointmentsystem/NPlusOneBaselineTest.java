package com.hospital.appointmentsystem;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.springframework.test.context.ActiveProfiles;

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
        appointmentService.getAppointmentsByPatientId(1L, null, null, null, org.springframework.data.domain.PageRequest.of(0, 100));
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed (Patient): " + queryCount);
        
        System.out.println("=== PATIENT APPOINTMENTS BASELINE END ===");
    }
    
    @Test
    public void testDoctorAppointmentsBaseline() {
        System.out.println("=== DOCTOR APPOINTMENTS BASELINE START ===");
        HibernateQueryInterceptor.startQueryCount();
        
        appointmentService.getAppointmentsByDoctorId(1L, null, null, null, org.springframework.data.domain.PageRequest.of(0, 100));
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed: " + queryCount);
        System.out.println("=== DOCTOR APPOINTMENTS BASELINE END ===");
    }
    
    @Test
    public void testDepartmentDoctorsBaseline() {
        System.out.println("=== DEPARTMENT DOCTORS BASELINE START ===");
        HibernateQueryInterceptor.startQueryCount();
        
        doctorService.getDoctorsByDepartmentId(1L);
        
        int queryCount = HibernateQueryInterceptor.getQueryCount();
        System.out.println("Total Queries Executed: " + queryCount);
        System.out.println("=== DEPARTMENT DOCTORS BASELINE END ===");
    }
}
