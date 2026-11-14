package com.hospital.appointmentsystem.doctor.impl;

import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorRegistrationService;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.security.passwordreset.EmailService;
import com.hospital.appointmentsystem.user.api.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class DoctorRegistrationServiceImpl implements DoctorRegistrationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TEMP_PASSWORD_BYTES = 32;

    private final DoctorRegistrationRequestRepository repository;
    private final DoctorService doctorService;
    private final UserService userService;
    private final EmailService emailService;

    public DoctorRegistrationServiceImpl(DoctorRegistrationRequestRepository repository,
                                         DoctorService doctorService,
                                         UserService userService,
                                         EmailService emailService) {
        this.repository = repository;
        this.doctorService = doctorService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void submit(SubmitCommand command) {
        DoctorRegistrationRequest request = new DoctorRegistrationRequest();
        request.setTcIdentityNumber(command.tcIdentityNumber());
        request.setFirstName(command.firstName());
        request.setLastName(command.lastName());
        request.setEmail(command.email());
        request.setPhoneNumber(command.phoneNumber());
        request.setDepartmentId(command.departmentId());
        request.setSpecialization(command.specialization());
        // The real credential is generated on approval; the column is NOT NULL in the schema.
        request.setPassword("");
        repository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationRequestView> getPendingRequests() {
        return repository.findByStatusOrderByRequestDateDesc(DoctorRegistrationStatus.PENDING).stream()
                .map(DoctorRegistrationServiceImpl::toView)
                .toList();
    }

    @Override
    @Transactional
    public void approve(Long requestId) {
        DoctorRegistrationRequest request = findPending(requestId);

        DoctorDto dto = new DoctorDto();
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setTcIdentityNumber(request.getTcIdentityNumber());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setEmail(request.getEmail());
        dto.setDepartmentId(request.getDepartmentId());
        dto.setSpecialization(request.getSpecialization());
        // Also creates the linked user account. A duplicate TC or e-mail raises
        // DataIntegrityViolationException: the transaction rolls back and the global handler returns 409.
        doctorService.createDoctor(dto);

        String tempPassword = generateTemporaryPassword();
        userService.changePassword(request.getTcIdentityNumber(), tempPassword);
        userService.setNeedsPasswordChange(request.getTcIdentityNumber(), true);

        request.setStatus(DoctorRegistrationStatus.APPROVED);

        // Send the credential only after the account is committed.
        String email = request.getEmail();
        String username = request.getTcIdentityNumber();
        afterCommit(() -> emailService.sendDoctorCredentialsEmail(email, username, tempPassword));
    }

    @Override
    @Transactional
    public void reject(Long requestId) {
        findPending(requestId).setStatus(DoctorRegistrationStatus.REJECTED);
    }

    private DoctorRegistrationRequest findPending(Long requestId) {
        DoctorRegistrationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorRegistrationRequest", "id", requestId));
        if (request.getStatus() != DoctorRegistrationStatus.PENDING) {
            throw new BusinessRuleException("Bu istek zaten işlem görmüş.");
        }
        return request;
    }

    private static String generateTemporaryPassword() {
        byte[] secret = new byte[TEMP_PASSWORD_BYTES];
        SECURE_RANDOM.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private static RegistrationRequestView toView(DoctorRegistrationRequest r) {
        return new RegistrationRequestView(r.getId(), r.getTcIdentityNumber(), r.getFirstName(),
                r.getLastName(), r.getEmail(), r.getPhoneNumber(), r.getSpecialization(),
                r.getDepartmentId(), r.getStatus().name(), r.getRequestDate());
    }
}
