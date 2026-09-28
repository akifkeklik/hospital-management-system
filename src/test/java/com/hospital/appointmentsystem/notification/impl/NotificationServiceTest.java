package com.hospital.appointmentsystem.notification.impl;

import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.notification.api.NotificationDto;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_success() {
        notificationService.createNotification(1L, 2L, "Test Message");
        
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotificationsByPatient_returnsList() {
        Notification n = new Notification(1L, 2L, "Msg");
        n.setId(10L);
        when(notificationRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n));

        List<NotificationDto> result = notificationService.getNotificationsByPatient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Msg");
    }

    @Test
    void broadcastToDoctors_savesNotificationForEachDoctor() {
        Doctor d1 = new Doctor();
        ReflectionTestUtils_setId(d1, 1L);
        Doctor d2 = new Doctor();
        ReflectionTestUtils_setId(d2, 2L);

        when(doctorRepository.findAll()).thenReturn(List.of(d1, d2));

        notificationService.broadcastToDoctors("System Update");

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void broadcastToAll_savesNotificationForEachUser() {
        Doctor d1 = new Doctor();
        ReflectionTestUtils_setId(d1, 1L);
        Patient p1 = new Patient();
        ReflectionTestUtils_setId(p1, 1L);

        when(doctorRepository.findAll()).thenReturn(List.of(d1));
        when(patientRepository.findAll()).thenReturn(List.of(p1));

        notificationService.broadcastToAll("System Update");

        // 1 for doctor, 1 for patient, 1 for admin
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void markAsRead_success() {
        Notification n = new Notification();
        n.setRead(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(1L);

        assertThat(n.isRead()).isTrue();
        verify(notificationRepository, times(1)).save(n);
    }

    @Test
    void markAsRead_notFound_throwsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUnreadCount_returnsCorrectNumber() {
        when(notificationRepository.findByPatientIdAndIsReadFalse(1L)).thenReturn(List.of(new Notification(), new Notification()));
        
        long count = notificationService.getUnreadCountForPatient(1L);
        
        assertThat(count).isEqualTo(2);
    }
    
    // A small helper to mimic ReflectionTestUtils for entities since it isn't imported here
    private void ReflectionTestUtils_setId(Object entity, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", id);
    }
}
