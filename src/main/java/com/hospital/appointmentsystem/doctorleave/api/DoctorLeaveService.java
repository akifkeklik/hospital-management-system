package com.hospital.appointmentsystem.doctorleave.api;

import java.util.List;

public interface DoctorLeaveService {
    List<DoctorLeaveDto> getAllLeaves();
    List<DoctorLeaveDto> getLeavesByDoctorId(Long doctorId);
    DoctorLeaveDto createLeave(DoctorLeaveDto dto);
    DoctorLeaveDto updateLeaveStatus(Long id, String status);
    void deleteLeave(Long id);
}
