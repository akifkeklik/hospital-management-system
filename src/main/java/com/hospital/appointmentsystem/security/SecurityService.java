package com.hospital.appointmentsystem.security;

import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    private final EntityManager entityManager;

    public SecurityService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public boolean isPatientOwner(Long patientId) {
        return isOwner(patientId, "ROLE_PATIENT");
    }

    public boolean isDoctorOwner(Long doctorId) {
        return isOwner(doctorId, "ROLE_DOCTOR");
    }

    public boolean isLeaveOwner(Long leaveId) {
        Long doctorId = getReferenceIdForRole("ROLE_DOCTOR");
        if (doctorId == null) return false;

        Long count = entityManager.createQuery(
                "SELECT COUNT(l) FROM DoctorLeave l WHERE l.id = :id AND l.doctor.id = :doctorId", Long.class)
                .setParameter("id", leaveId)
                .setParameter("doctorId", doctorId)
                .getSingleResult();
        return count > 0;
    }

    public boolean isNotificationOwner(Long notificationId) {
        Long patientId = getReferenceIdForRole("ROLE_PATIENT");
        Long doctorId = getReferenceIdForRole("ROLE_DOCTOR");
        if (patientId == null && doctorId == null) return false;

        String jpql = "SELECT COUNT(n) FROM Notification n WHERE n.id = :id AND ";
        if (patientId != null) {
            jpql += "n.patientId = :refId";
            return entityManager.createQuery(jpql, Long.class)
                    .setParameter("id", notificationId)
                    .setParameter("refId", patientId)
                    .getSingleResult() > 0;
        } else {
            jpql += "n.doctorId = :refId";
            return entityManager.createQuery(jpql, Long.class)
                    .setParameter("id", notificationId)
                    .setParameter("refId", doctorId)
                    .getSingleResult() > 0;
        }
    }

    public boolean isPatientOfAppointment(Long appointmentId) {
        Long patientId = getReferenceIdForRole("ROLE_PATIENT");
        if (patientId == null) return false;

        return entityManager.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.id = :id AND a.patient.id = :patientId", Long.class)
                .setParameter("id", appointmentId)
                .setParameter("patientId", patientId)
                .getSingleResult() > 0;
    }

    public boolean isDoctorOfAppointment(Long appointmentId) {
        Long doctorId = getReferenceIdForRole("ROLE_DOCTOR");
        if (doctorId == null) return false;

        return entityManager.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.id = :id AND a.doctor.id = :doctorId", Long.class)
                .setParameter("id", appointmentId)
                .setParameter("doctorId", doctorId)
                .getSingleResult() > 0;
    }

    public boolean isDoctorOfDiagnosis(Long diagnosisId) {
        Long doctorId = getReferenceIdForRole("ROLE_DOCTOR");
        if (doctorId == null) return false;

        return entityManager.createQuery(
                "SELECT COUNT(d) FROM Diagnosis d WHERE d.id = :id AND d.appointment.doctor.id = :doctorId", Long.class)
                .setParameter("id", diagnosisId)
                .setParameter("doctorId", doctorId)
                .getSingleResult() > 0;
    }

    public boolean isDoctorOfPrescription(Long prescriptionId) {
        Long doctorId = getReferenceIdForRole("ROLE_DOCTOR");
        if (doctorId == null) return false;

        return entityManager.createQuery(
                "SELECT COUNT(p) FROM Prescription p WHERE p.id = :id AND p.appointment.doctor.id = :doctorId", Long.class)
                .setParameter("id", prescriptionId)
                .setParameter("doctorId", doctorId)
                .getSingleResult() > 0;
    }

    private boolean isOwner(Long referenceId, String expectedRole) {
        Long actualReferenceId = getReferenceIdForRole(expectedRole);
        return actualReferenceId != null && actualReferenceId.equals(referenceId);
    }

    private Long getReferenceIdForRole(String expectedRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            if (expectedRole.equals(customUserDetails.getRole())) {
                return customUserDetails.getReferenceId();
            }
        }
        return null;
    }
}
