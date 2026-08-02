package com.hospital.appointmentsystem.notification.impl;

import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.notification.api.NotificationDto;
import com.hospital.appointmentsystem.notification.api.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.notificationRepository = notificationRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void createNotification(Long patientId, Long doctorId, String message) {
        Notification notification = new Notification(patientId, doctorId, message);
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDto> getNotificationsByPatient(Long patientId) {
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(n -> new NotificationDto(n.getId(), n.getPatientId(), n.getDoctorId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(n -> new NotificationDto(n.getId(), n.getPatientId(), n.getDoctorId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void broadcastToDoctors(String message) {
        List<Doctor> doctors = doctorRepository.findAll();
        for (Doctor doc : doctors) {
            Notification notification = new Notification(null, doc.getId(), message);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void broadcastToAll(String message) {
        List<Doctor> doctors = doctorRepository.findAll();
        for (Doctor doc : doctors) {
            Notification notification = new Notification(null, doc.getId(), message);
            notificationRepository.save(notification);
        }
        
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            Notification notification = new Notification(patient.getId(), null, message);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public long getUnreadCountForPatient(Long patientId) {
        return notificationRepository.findByPatientIdAndIsReadFalse(patientId).size();
    }

    @Override
    public long getUnreadCountForDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdAndIsReadFalse(doctorId).size();
    }
}
