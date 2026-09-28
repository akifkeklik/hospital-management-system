package com.hospital.appointmentsystem.doctorleave.impl;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.doctorleave.api.DoctorLeaveDto;
import com.hospital.appointmentsystem.notification.api.NotificationService;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorLeaveServiceTest {

    @Mock
    private DoctorLeaveRepository doctorLeaveRepository;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DoctorLeaveServiceImpl doctorLeaveService;

    private DoctorLeave mockLeave;

    @BeforeEach
    void setUp() {
        mockLeave = new DoctorLeave(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "Hastalık izni");
        mockLeave.setId(10L);
        mockLeave.setStatus("PENDING");
    }

    @Test
    void updateLeaveStatus_statusNotApproved_doesNotCancelAppointments() {
        when(doctorLeaveRepository.findById(10L)).thenReturn(Optional.of(mockLeave));
        when(doctorLeaveRepository.save(any(DoctorLeave.class))).thenAnswer(i -> i.getArgument(0));

        DoctorLeaveDto result = doctorLeaveService.updateLeaveStatus(10L, "REJECTED");

        assertThat(result.getStatus()).isEqualTo("REJECTED");

        verify(appointmentService, never()).getAppointmentsByDoctorId(any(), any(), any(), any(), any());
        verify(appointmentService, never()).cancelAppointment(any());
        verify(notificationService, never()).createNotification(any(), any(), any());
    }

    @Test
    void updateLeaveStatus_statusApproved_cancelsAppointmentsAndNotifiesPatients() {
        when(doctorLeaveRepository.findById(10L)).thenReturn(Optional.of(mockLeave));
        when(doctorLeaveRepository.save(any(DoctorLeave.class))).thenAnswer(i -> i.getArgument(0));

        // Create mock appointments: one overlapping, one outside range, one already cancelled
        AppointmentDto overlappingApp = new AppointmentDto();
        overlappingApp.setId(100L);
        overlappingApp.setDoctorId(1L);
        overlappingApp.setPatientId(200L);
        overlappingApp.setStatus("SCHEDULED");
        overlappingApp.setAppointmentDate(LocalDateTime.now().plusDays(2).withHour(10));

        AppointmentDto outsideApp = new AppointmentDto();
        outsideApp.setId(101L);
        outsideApp.setStatus("SCHEDULED");
        outsideApp.setAppointmentDate(LocalDateTime.now().plusDays(5).withHour(10));

        AppointmentDto cancelledApp = new AppointmentDto();
        cancelledApp.setId(102L);
        cancelledApp.setStatus("CANCELLED");
        cancelledApp.setAppointmentDate(LocalDateTime.now().plusDays(2).withHour(11));

        Page<AppointmentDto> page = new PageImpl<>(List.of(overlappingApp, outsideApp, cancelledApp));

        when(appointmentService.getAppointmentsByDoctorId(eq(1L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        DoctorLeaveDto result = doctorLeaveService.updateLeaveStatus(10L, "APPROVED");

        assertThat(result.getStatus()).isEqualTo("APPROVED");

        // Only overlappingApp should be cancelled
        verify(appointmentService, times(1)).cancelAppointment(100L);
        verify(appointmentService, never()).cancelAppointment(101L);
        verify(appointmentService, never()).cancelAppointment(102L);

        // Notification should be sent for overlappingApp
        verify(notificationService, times(1)).createNotification(
                eq(200L), eq(1L), contains("randevunuz iptal edilmiştir")
        );
    }

    @Test
    void updateLeaveStatus_notFound_throwsException() {
        when(doctorLeaveRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorLeaveService.updateLeaveStatus(10L, "APPROVED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DoctorLeave");
    }
}
