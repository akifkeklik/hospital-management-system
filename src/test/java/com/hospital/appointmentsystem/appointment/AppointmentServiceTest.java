package com.hospital.appointmentsystem.appointment;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.WaitTimeDto;
import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentServiceImpl;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.doctorleave.impl.DoctorLeaveRepository;
import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.setting.api.SystemSettingDto;
import com.hospital.appointmentsystem.setting.api.SystemSettingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private DoctorLeaveRepository doctorLeaveRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private MockedStatic<LocalDate> mockedLocalDate;
    private MockedStatic<LocalTime> mockedLocalTime;
    private MockedStatic<LocalDateTime> mockedLocalDateTime;

    private final LocalDate FIXED_DATE = LocalDate.of(2026, 8, 25);
    private final LocalTime FIXED_TIME = LocalTime.of(10, 0); // 10:00 AM
    private final LocalDateTime FIXED_DATETIME = LocalDateTime.of(FIXED_DATE, FIXED_TIME);

    private Doctor mockDoctor;
    private Patient mockPatient;
    private SystemSettingDto mockSettings;

    @BeforeEach
    void setUp() {
        // Fix the time to ensure deterministic tests
        mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(FIXED_DATE);

        mockedLocalTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalTime.when(LocalTime::now).thenReturn(FIXED_TIME);

        mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(FIXED_DATETIME);

        mockDoctor = new Doctor();
        mockDoctor.setId(1L);

        mockPatient = new Patient();
        mockPatient.setId(1L);

        mockSettings = new SystemSettingDto();
        mockSettings.setWorkStartTime("09:00");
        mockSettings.setWorkEndTime("17:00");
        mockSettings.setLunchBreakStart("12:00");
        mockSettings.setLunchBreakEnd("13:00");
        mockSettings.setAppointmentDuration(30);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDate.close();
        mockedLocalTime.close();
        mockedLocalDateTime.close();
    }

    // ==========================================
    // AVAILABLE SLOTS TESTS
    // ==========================================

    @Test
    void shouldReturnAvailableSlots_WhenDoctorIsAvailable() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, FIXED_DATE)).thenReturn(false);
        when(systemSettingService.getSettings()).thenReturn(mockSettings);
        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        List<String> slots = appointmentService.getAvailableSlots(1L, FIXED_DATE);

        // 09:00 to 17:00 with 30 min duration = 16 slots. Lunch 12:00 to 13:00 removes 2 slots.
        // But since FIXED_TIME is 10:00, slots before 10:00 should be excluded (09:00, 09:30).
        // Let's verify expectations:
        assertFalse(slots.contains("09:00"));
        assertFalse(slots.contains("09:30"));
        assertTrue(slots.contains("10:00"));
        assertTrue(slots.contains("10:30"));
        assertTrue(slots.contains("11:30"));
    }

    @Test
    void shouldNotReturnSlotsDuringLunchBreak() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, FIXED_DATE.plusDays(1))).thenReturn(false);
        when(systemSettingService.getSettings()).thenReturn(mockSettings);
        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Testing for tomorrow so past time filter doesn't apply to morning
        List<String> slots = appointmentService.getAvailableSlots(1L, FIXED_DATE.plusDays(1));

        assertTrue(slots.contains("11:30"));
        assertFalse(slots.contains("12:00")); // Lunch start
        assertFalse(slots.contains("12:30")); // Lunch
        assertTrue(slots.contains("13:00"));  // Lunch end
    }

    @Test
    void shouldReturnEmptyListIfDoctorOnLeave() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, FIXED_DATE)).thenReturn(true);

        List<String> slots = appointmentService.getAvailableSlots(1L, FIXED_DATE);

        assertTrue(slots.isEmpty());
        // Verify we didn't even check settings or DB for appointments because leave cancels all
        verify(systemSettingService, never()).getSettings();
        verify(appointmentRepository, never()).findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any());
    }

    @Test
    void shouldNotReturnOccupiedSlots() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, FIXED_DATE.plusDays(1))).thenReturn(false);
        when(systemSettingService.getSettings()).thenReturn(mockSettings);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE.plusDays(1), LocalTime.of(14, 0)));
        List<Appointment> existing = List.of(existingAppointment);

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any()))
                .thenReturn(existing);

        List<String> slots = appointmentService.getAvailableSlots(1L, FIXED_DATE.plusDays(1));

        assertTrue(slots.contains("13:30"));
        assertFalse(slots.contains("14:00")); // Occupied!
        assertTrue(slots.contains("14:30"));
    }

    // ==========================================
    // APPOINTMENT CREATION TESTS
    // ==========================================

    @Test
    void shouldCreateAppointmentSuccessfully() {
        AppointmentDto request = new AppointmentDto();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        // 14:00 is a valid slot, next day
        request.setAppointmentDate(LocalDateTime.of(FIXED_DATE.plusDays(1), LocalTime.of(14, 0)));
        request.setNotes("Test");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(mockDoctor));
        
        // Mock getAvailableSlots dependencies
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, request.getAppointmentDate().toLocalDate())).thenReturn(false);
        when(systemSettingService.getSettings()).thenReturn(mockSettings);
        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
                
        // Mock save
        Appointment savedAppt = new Appointment();
        savedAppt.setId(99L);
        savedAppt.setDoctor(mockDoctor);
        savedAppt.setPatient(mockPatient);
        savedAppt.setAppointmentDate(request.getAppointmentDate());
        savedAppt.setStatus(AppointmentStatus.SCHEDULED);
        
        // We have to mock Department inside doctor for DTO mapping
        com.hospital.appointmentsystem.department.impl.Department dept = new com.hospital.appointmentsystem.department.impl.Department();
        dept.setName("Cardiology");
        mockDoctor.setDepartment(dept);

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppt);

        AppointmentDto result = appointmentService.createAppointment(request);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("SCHEDULED", result.getStatus());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingAppointmentInOccupiedSlot() {
        AppointmentDto request = new AppointmentDto();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDateTime.of(FIXED_DATE.plusDays(1), LocalTime.of(14, 0)));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(mockDoctor));
        
        // Mock getAvailableSlots dependencies
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(doctorLeaveRepository.isDoctorOnLeave(1L, request.getAppointmentDate().toLocalDate())).thenReturn(false);
        when(systemSettingService.getSettings()).thenReturn(mockSettings);
        
        // Occupy 14:00
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentDate(request.getAppointmentDate());
        List<Appointment> existing = List.of(existingAppointment);
        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(any(), any(), any(), any()))
                .thenReturn(existing);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            appointmentService.createAppointment(request);
        });

        assertTrue(ex.getMessage().contains("dolu veya mesai saatleri dışında"));
        verify(appointmentRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        AppointmentDto request = new AppointmentDto();
        request.setPatientId(999L);
        
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(request);
        });
        
        assertEquals("Patient not found with id: 999", ex.getMessage());
    }

    // ==========================================
    // APPOINTMENT STATUS UPDATE TESTS
    // ==========================================

    @Test
    void shouldThrowExceptionWhenMarkingFutureAppointmentAsCompleted() {
        Appointment futureAppointment = new Appointment();
        futureAppointment.setId(1L);
        // Future date relative to FIXED_DATETIME
        futureAppointment.setAppointmentDate(FIXED_DATETIME.plusDays(2));
        futureAppointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            appointmentService.updateAppointmentStatus(1L, "COMPLETED");
        });

        assertTrue(ex.getMessage().contains("Gelecekteki bir randevu 'Tamamlandı' veya 'Gelmedi' olarak işaretlenemez!"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenMarkingFutureAppointmentAsNoShow() {
        Appointment futureAppointment = new Appointment();
        futureAppointment.setId(1L);
        futureAppointment.setAppointmentDate(FIXED_DATETIME.plusDays(2));
        futureAppointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(futureAppointment));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            appointmentService.updateAppointmentStatus(1L, "NO_SHOW");
        });

        assertTrue(ex.getMessage().contains("Gelecekteki bir randevu"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldAllowMarkingPastAppointmentAsCompleted() {
        Appointment pastAppointment = new Appointment();
        pastAppointment.setId(1L);
        // Past date relative to FIXED_DATETIME
        pastAppointment.setAppointmentDate(FIXED_DATETIME.minusDays(1));
        pastAppointment.setStatus(AppointmentStatus.SCHEDULED);
        
        pastAppointment.setDoctor(mockDoctor);
        pastAppointment.setPatient(mockPatient);
        com.hospital.appointmentsystem.department.impl.Department dept = new com.hospital.appointmentsystem.department.impl.Department();
        dept.setName("TestDept");
        mockDoctor.setDepartment(dept);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pastAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(pastAppointment);

        AppointmentDto result = appointmentService.updateAppointmentStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    // ==========================================
    // ESTIMATED WAIT TIME TESTS
    // ==========================================

    @Test
    void scenarioA_shouldCalculateQueuePositionCorrectly() {
        // Arrange: Multiple SCHEDULED appointments on the same day.
        Appointment targetAppointment = new Appointment();
        targetAppointment.setId(10L);
        targetAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(14, 0))); // 14:00
        targetAppointment.setDoctor(mockDoctor);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(targetAppointment));

        List<Appointment> sameDayAppointments = new ArrayList<>();
        // 3 past appointments (Queue position = 3)
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 0))));
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(10, 0))));
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(11, 0))));
        // 1 future appointment that is AFTER the target (should not be counted in queue)
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(15, 0))));
        sameDayAppointments.add(targetAppointment);

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(
                eq(1L), eq(AppointmentStatus.SCHEDULED), any(), any()))
                .thenReturn(sameDayAppointments);

        // Act
        WaitTimeDto result = appointmentService.getEstimatedWaitTime(10L);

        // Assert
        // Queue position calculation: 3 past appointments. Wait time = 3 * 15 = 45 mins.
        // Current time is 10:00. Time until appointment is 4 hours (240 mins).
        // Correct business rule: Math.max(240, 45) = 240 mins.
        assertEquals(240, result.getEstimatedMinutes());
        assertEquals(4, result.getQueuePosition()); // The response queue position is queuePosition + 1
    }

    @Test
    void scenarioB_shouldReturnQueueTime_WhenQueueTakesLongerThanTimeUntilAppointment_FutureAppointment() {
        // Arrange
        // This is the regression test for the bug!
        // Current time: 10:00 (FIXED_TIME)
        // Appointment: 10:10 (10 minutes away)
        // Queue: 3 prior appointments still SCHEDULED (3 * 15 = 45 minutes of work)
        // Expected Wait Time: Math.max(10, 45) = 45 minutes

        Appointment currentAppointment = new Appointment();
        currentAppointment.setId(20L);
        currentAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(10, 10)));
        currentAppointment.setDoctor(mockDoctor);

        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(currentAppointment));

        List<Appointment> sameDayAppointments = new ArrayList<>();
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 0))));
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 15))));
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 30))));
        sameDayAppointments.add(currentAppointment);

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(
                eq(1L), eq(AppointmentStatus.SCHEDULED), any(), any()))
                .thenReturn(sameDayAppointments);

        // Act
        WaitTimeDto result = appointmentService.getEstimatedWaitTime(20L);

        // Assert
        // Buggy code returns 10. Corrected code returns 45.
        assertEquals(45, result.getEstimatedMinutes(), "Wait time should consider queue length if it's longer than time until appointment");
    }

    @Test
    void scenarioC_shouldReturnQueueTime_ForPastAppointment() {
        // Arrange
        // Current time: 10:00 (FIXED_TIME)
        // Appointment: 09:30 (Past appointment)
        // The patient is late, or the doctor is very late.
        // Queue position: 2 (08:30, 09:00) -> 30 mins

        Appointment currentAppointment = new Appointment();
        currentAppointment.setId(30L);
        currentAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 30)));
        currentAppointment.setDoctor(mockDoctor);

        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(currentAppointment));

        List<Appointment> sameDayAppointments = new ArrayList<>();
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(8, 30))));
        sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 0))));
        sameDayAppointments.add(currentAppointment);

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(
                eq(1L), eq(AppointmentStatus.SCHEDULED), any(), any()))
                .thenReturn(sameDayAppointments);

        // Act
        WaitTimeDto result = appointmentService.getEstimatedWaitTime(30L);

        // Assert
        // Since the appointment time is in the past, now.isBefore() is false.
        // It strictly uses queuePosition * 15.
        assertEquals(30, result.getEstimatedMinutes());
    }

    @Test
    void scenarioD_shouldReturnCorrectWaitTime_WhenNoPreviousAppointment() {
        // Arrange
        Appointment currentAppointment = new Appointment();
        currentAppointment.setId(40L);
        currentAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(10, 20))); // 20 mins away
        currentAppointment.setDoctor(mockDoctor);

        when(appointmentRepository.findById(40L)).thenReturn(Optional.of(currentAppointment));

        List<Appointment> sameDayAppointments = new ArrayList<>();
        sameDayAppointments.add(currentAppointment); // Only this appointment

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(
                eq(1L), eq(AppointmentStatus.SCHEDULED), any(), any()))
                .thenReturn(sameDayAppointments);

        // Act
        WaitTimeDto result = appointmentService.getEstimatedWaitTime(40L);

        // Assert
        // Queue position = 0 (Wait = 0).
        // Minutes until appointment = 20.
        // Math.max(20, 0) = 20.
        assertEquals(20, result.getEstimatedMinutes());
        assertEquals(1, result.getQueuePosition());
    }

    @Test
    void scenarioE_shouldMapBusyLevelsCorrectly() {
        // Create a method to test the busy level easily
        assertEquals("LOW", evaluateBusyLevel(5));
        assertEquals("MEDIUM", evaluateBusyLevel(6));
        assertEquals("MEDIUM", evaluateBusyLevel(12));
        assertEquals("HIGH", evaluateBusyLevel(13));
    }

    private String evaluateBusyLevel(int totalAppointments) {
        Appointment currentAppointment = new Appointment();
        currentAppointment.setId(100L);
        currentAppointment.setAppointmentDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(9, 0)));
        currentAppointment.setDoctor(mockDoctor);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(currentAppointment));

        List<Appointment> sameDayAppointments = new ArrayList<>();
        for (int i = 0; i < totalAppointments; i++) {
            sameDayAppointments.add(createAppointmentWithDate(LocalDateTime.of(FIXED_DATE, LocalTime.of(1, 0).plusMinutes(i * 10))));
        }

        when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetween(
                eq(1L), eq(AppointmentStatus.SCHEDULED), any(), any()))
                .thenReturn(sameDayAppointments);

        return appointmentService.getEstimatedWaitTime(100L).getBusyLevel();
    }

    private Appointment createAppointmentWithDate(LocalDateTime date) {
        Appointment a = new Appointment();
        a.setAppointmentDate(date);
        a.setDoctor(mockDoctor);
        return a;
    }
}
