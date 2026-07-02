package com.hospital.appointmentsystem.notification.web;

import com.hospital.appointmentsystem.notification.api.NotificationDto;
import com.hospital.appointmentsystem.notification.api.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NotificationDto>> getPatientNotifications(@PathVariable Long patientId) {
        return ResponseEntity.ok(notificationService.getNotificationsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long patientId) {
        return ResponseEntity.ok(notificationService.getUnreadCountForPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<NotificationDto>> getDoctorNotifications(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getNotificationsByDoctor(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/unread-count")
    public ResponseEntity<Long> getDoctorUnreadCount(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getUnreadCountForDoctor(doctorId));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcastToDoctors(@RequestBody String message) {
        notificationService.broadcastToDoctors(message);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
