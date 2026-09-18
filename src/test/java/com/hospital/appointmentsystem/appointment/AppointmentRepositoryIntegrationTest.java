package com.hospital.appointmentsystem.appointment;

import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AppointmentRepositoryIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        departmentRepository.deleteAll();

        Department dept = new Department();
        dept.setName("Unique Integration Dept");
        dept = departmentRepository.save(dept);

        Doctor d = new Doctor();
        d.setFirstName("Ahmet");
        d.setLastName("Yılmaz");
        d.setSpecialization("Dahiliye");
        d.setTcIdentityNumber("11111111111");
        d.setDepartment(dept);
        d = doctorRepository.save(d);

        Patient p = new Patient();
        p.setFirstName("Mehmet");
        p.setLastName("Kaya");
        p.setTcIdentityNumber("22222222222");
        p.setPhoneNumber("5551234567");
        p.setEmail("m@m.com");
        p = patientRepository.save(p);

        // Day 1: 3 appointments
        LocalDateTime day1 = LocalDateTime.now().plusDays(1).with(LocalTime.of(10, 0));
        appointmentRepository.save(createAppt(d, p, day1));
        appointmentRepository.save(createAppt(d, p, day1.plusHours(1)));
        appointmentRepository.save(createAppt(d, p, day1.plusHours(2)));

        // Day 2: 2 appointments
        LocalDateTime day2 = LocalDateTime.now().plusDays(2).with(LocalTime.of(10, 0));
        appointmentRepository.save(createAppt(d, p, day2));
        appointmentRepository.save(createAppt(d, p, day2.plusHours(1)));

        // Day 3 (past): 1 appointment
        LocalDateTime day3 = LocalDateTime.now().minusDays(1).with(LocalTime.of(10, 0));
        appointmentRepository.save(createAppt(d, p, day3));
    }

    private Appointment createAppt(Doctor d, Patient p, LocalDateTime date) {
        Appointment a = new Appointment();
        a.setDoctor(d);
        a.setPatient(p);
        a.setAppointmentDate(date);
        a.setStatus(AppointmentStatus.SCHEDULED);
        a.setNotes("Test");
        return a;
    }

    @Test
    void getAppointmentsByDate_ShouldGroupAndCountByDate_AndTestCastFunction() {
        // Test CAST(a.appointmentDate AS date) behavior in H2.
        // If start date is 'today', it should return Day 1 (count 3) and Day 2 (count 2).
        LocalDateTime startDate = LocalDateTime.now().with(LocalTime.MIN);
        
        List<Object[]> results = appointmentRepository.getAppointmentsByDate(startDate);

        // Expecting 2 rows (Day 1 and Day 2)
        assertEquals(2, results.size());

        // We don't know exact order guaranteed by H2's object return, but the query uses ORDER BY CAST(a.appointmentDate AS date)
        
        // First row should be day1
        Object[] row1 = results.get(0);
        assertEquals(java.sql.Date.valueOf(LocalDate.now().plusDays(1)), row1[0]);
        assertEquals(3L, ((Number) row1[1]).longValue());

        // Second row should be day2
        Object[] row2 = results.get(1);
        assertEquals(java.sql.Date.valueOf(LocalDate.now().plusDays(2)), row2[0]);
        assertEquals(2L, ((Number) row2[1]).longValue());
    }
}
