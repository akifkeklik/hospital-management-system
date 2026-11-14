package com.hospital.appointmentsystem.security;

import com.hospital.appointmentsystem.doctor.api.DoctorRegistrationService;
import com.hospital.appointmentsystem.exception.DuplicateAccountException;
import com.hospital.appointmentsystem.patient.api.PatientDto;
import com.hospital.appointmentsystem.patient.api.PatientService;
import com.hospital.appointmentsystem.user.api.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Self-service sign-up for patients and doctors. The TC identity number is used as the username.
 */
@Service
public class AccountRegistrationService {

    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorRegistrationService doctorRegistrationService;

    public AccountRegistrationService(UserService userService, PatientService patientService,
                                      DoctorRegistrationService doctorRegistrationService) {
        this.userService = userService;
        this.patientService = patientService;
        this.doctorRegistrationService = doctorRegistrationService;
    }

    /** Creates the patient record and its login in one transaction, so a failure leaves no orphan patient. */
    @Transactional
    public void registerPatient(String tcIdentityNumber, String firstName, String lastName, String email,
                                String phoneNumber, String password) {
        ensureAccountIsFree(tcIdentityNumber, email);

        PatientDto saved = patientService.createPatient(
                new PatientDto(null, firstName, lastName, tcIdentityNumber, phoneNumber, email));
        userService.registerUser(tcIdentityNumber, email, password, ROLE_PATIENT, saved.getId());
    }

    @Transactional
    public void submitDoctorRegistration(DoctorRegistrationService.SubmitCommand command) {
        ensureAccountIsFree(command.tcIdentityNumber(), command.email());
        doctorRegistrationService.submit(command);
    }

    private void ensureAccountIsFree(String tcIdentityNumber, String email) {
        if (userService.existsByUsername(tcIdentityNumber)) {
            throw new DuplicateAccountException("Hata: Bu TC Kimlik Numarası zaten kayıtlı.");
        }
        if (userService.existsByEmail(email)) {
            throw new DuplicateAccountException("Hata: Bu e-posta adresi zaten kullanılıyor.");
        }
    }
}
