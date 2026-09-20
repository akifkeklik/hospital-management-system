package com.hospital.appointmentsystem.notification.api;

import java.util.List;

public interface NotificationService {
    void createNotification(Long patientId, Long doctorId, String message);
    List<NotificationDto> getNotificationsByPatient(Long patientId);
    List<NotificationDto> getNotificationsByDoctor(Long doctorId);
    List<NotificationDto> getNotificationsByAdmin(Long adminId);
    void broadcastToDoctors(String message);
    void broadcastToAll(String message);
    void markAsRead(Long notificationId);
    long getUnreadCountForPatient(Long patientId);
    long getUnreadCountForDoctor(Long doctorId);
    long getUnreadCountForAdmin(Long adminId);
}
