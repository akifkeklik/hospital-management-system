package com.hospital.appointmentsystem.doctorleave.impl;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.notification.api.NotificationService;
import com.hospital.appointmentsystem.doctorleave.api.DoctorLeaveDto;
import com.hospital.appointmentsystem.doctorleave.api.DoctorLeaveService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorLeaveServiceImpl implements DoctorLeaveService {

    private final DoctorLeaveRepository repository;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    public DoctorLeaveServiceImpl(DoctorLeaveRepository repository,
                                  AppointmentService appointmentService,
                                  NotificationService notificationService) {
        this.repository = repository;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
    }

    @Override
    public List<DoctorLeaveDto> getAllLeaves() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<DoctorLeaveDto> getLeavesByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public DoctorLeaveDto createLeave(DoctorLeaveDto dto) {
        DoctorLeave leave = new DoctorLeave(dto.getDoctorId(), dto.getStartDate(), dto.getEndDate(), dto.getReason());
        leave.setStatus("PENDING");
        leave = repository.save(leave);
        return mapToDto(leave);
    }

    @Override
    public DoctorLeaveDto updateLeaveStatus(Long id, String status) {
        DoctorLeave leave = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DoctorLeave", "id", id));
        leave.setStatus(status);
        leave = repository.save(leave);

        if ("APPROVED".equals(status)) {
            // Find scheduled appointments and cancel them
            // Sadece bekleyen randevuları al
            Page<AppointmentDto> appointmentsPage = appointmentService.getAppointmentsByDoctorId(leave.getDoctorId(), null, null, null, Pageable.unpaged());
            List<AppointmentDto> appointments = appointmentsPage.getContent();
            for (AppointmentDto app : appointments) {
                if ("SCHEDULED".equals(app.getStatus())) {
                    LocalDateTime appDate = app.getAppointmentDate();
                    if (!appDate.toLocalDate().isBefore(leave.getStartDate()) && !appDate.toLocalDate().isAfter(leave.getEndDate())) {
                        appointmentService.cancelAppointment(app.getId());
                        notificationService.createNotification(app.getPatientId(), leave.getDoctorId(),
                            "Hastane Başhekimliği: Sayın Hastamız, doktorumuzun izni sebebiyle " + appDate.toLocalDate() + " tarihindeki randevunuz iptal edilmiştir.");
                    }
                }
            }
        }

        return mapToDto(leave);
    }

    @Override
    public void deleteLeave(Long id) {
        repository.deleteById(id);
    }

    private DoctorLeaveDto mapToDto(DoctorLeave leave) {
        return new DoctorLeaveDto(leave.getId(), leave.getDoctorId(), leave.getStartDate(), leave.getEndDate(), leave.getReason(), leave.getStatus());
    }
}
