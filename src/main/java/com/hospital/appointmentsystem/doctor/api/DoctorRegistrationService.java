package com.hospital.appointmentsystem.doctor.api;

import java.time.LocalDateTime;
import java.util.List;

/** Doctor self-registration workflow: submit, then an admin approves or rejects. */
public interface DoctorRegistrationService {

    record SubmitCommand(String tcIdentityNumber, String firstName, String lastName, String email,
                         String phoneNumber, Long departmentId, String specialization) {
    }

    record RegistrationRequestView(Long id, String tcIdentityNumber, String firstName, String lastName,
                                   String email, String phoneNumber, String specialization,
                                   Long departmentId, String status, LocalDateTime requestDate) {
    }

    void submit(SubmitCommand command);

    List<RegistrationRequestView> getPendingRequests();

    /** Creates the doctor account with a temporary password and e-mails it to the doctor. */
    void approve(Long requestId);

    void reject(Long requestId);
}
