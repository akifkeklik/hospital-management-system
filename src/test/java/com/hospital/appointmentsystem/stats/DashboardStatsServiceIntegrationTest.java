package com.hospital.appointmentsystem.stats;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.stats.dto.DashboardStatsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DashboardStatsServiceIntegrationTest {

    @Autowired
    private DashboardStatsService dashboardStatsService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void testAppointmentsByDateAggregation() {
        // Clear existing appointments to test isolation
        appointmentRepository.deleteAllInBatch();

        Doctor doctor = doctorRepository.findById(1L).orElseThrow();
        Patient patient = patientRepository.findById(1L).orElseThrow();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        // 1. Appointment for today
        createAppointment(doctor, patient, today.atTime(10, 0));
        
        // 2. Appointment for 2 days ago (multiple to test count)
        createAppointment(doctor, patient, today.minusDays(2).atTime(9, 0));
        createAppointment(doctor, patient, today.minusDays(2).atTime(11, 0));

        // 3. Appointment for 8 days ago (should be excluded)
        createAppointment(doctor, patient, today.minusDays(8).atTime(14, 0));

        // Act
        DashboardStatsDTO stats = dashboardStatsService.getDashboardStats();

        // Assert
        Map<String, Long> apptsByDate = stats.getAppointmentsByDate();
        
        assertNotNull(apptsByDate);
        assertEquals(7, apptsByDate.size(), "Should always contain exactly 7 days");

        // Verify today
        String todayKey = today.format(formatter);
        assertTrue(apptsByDate.containsKey(todayKey));
        assertEquals(1L, apptsByDate.get(todayKey));

        // Verify 2 days ago
        String twoDaysAgoKey = today.minusDays(2).format(formatter);
        assertTrue(apptsByDate.containsKey(twoDaysAgoKey));
        assertEquals(2L, apptsByDate.get(twoDaysAgoKey));

        // Verify 1 day ago (should be 0)
        String oneDayAgoKey = today.minusDays(1).format(formatter);
        assertTrue(apptsByDate.containsKey(oneDayAgoKey));
        assertEquals(0L, apptsByDate.get(oneDayAgoKey));

        // Verify 8 days ago is NOT in the map
        String eightDaysAgoKey = today.minusDays(8).format(formatter);
        assertTrue(!apptsByDate.containsKey(eightDaysAgoKey), "Dates older than 7 days should not be present");
    }

    private void createAppointment(Doctor doctor, Patient patient, LocalDateTime dateTime) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(dateTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes("Integration Test");
        appointmentRepository.save(appointment);
        appointmentRepository.flush();
    }
}
