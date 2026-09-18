package com.hospital.appointmentsystem.stats;

import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.stats.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardStatsService {

    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DashboardStatsService(DepartmentRepository departmentRepository,
                                 PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 AppointmentRepository appointmentRepository) {
        this.departmentRepository = departmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. Basic Counts
        stats.setTotalDepartments(departmentRepository.count());
        stats.setTotalPatients(patientRepository.count());
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalAppointments(appointmentRepository.count());

        // 2. Doctor Distribution by Department
        List<Object[]> distributionResult = doctorRepository.getDoctorDistribution();
        Map<String, Long> distributionMap = new HashMap<>();
        if (distributionResult != null) {
            for (Object[] row : distributionResult) {
                if (row[0] != null && row[1] != null) {
                    distributionMap.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
        }
        stats.setDoctorDistribution(distributionMap);

        // 3. Appointments by Date (last 7 days, including today)
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = today.minusDays(6);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<Object[]> appointmentsResult = appointmentRepository.getAppointmentsByDate(startDateTime);
        Map<String, Long> appointmentsMap = new java.util.LinkedHashMap<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

        // Initialize 7 days with 0
        for (int i = 0; i < 7; i++) {
            appointmentsMap.put(startDate.plusDays(i).format(formatter), 0L);
        }

        if (appointmentsResult != null) {
            for (Object[] row : appointmentsResult) {
                if (row[0] != null && row[1] != null) {
                    String dateKey = row[0].toString();
                    if (dateKey.length() > 10) {
                        dateKey = dateKey.substring(0, 10);
                    }
                    if (appointmentsMap.containsKey(dateKey)) {
                        appointmentsMap.put(dateKey, ((Number) row[1]).longValue());
                    }
                }
            }
        }
        stats.setAppointmentsByDate(appointmentsMap);

        return stats;
    }
}
