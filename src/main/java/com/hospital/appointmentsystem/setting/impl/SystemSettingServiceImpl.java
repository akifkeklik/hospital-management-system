package com.hospital.appointmentsystem.setting.impl;

import com.hospital.appointmentsystem.setting.api.SystemSettingDto;
import com.hospital.appointmentsystem.setting.api.SystemSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingServiceImpl(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @Cacheable(value = "systemSettings")
    public SystemSettingDto getSettings() {
        SystemSetting setting = repository.findById(1L).orElseGet(() -> {
            SystemSetting defaultSetting = new SystemSetting(15, "09:00", "17:00", "12:00", "13:00", false);
            return repository.save(defaultSetting);
        });
        return mapToDto(setting);
    }

    @Override
    @Transactional
    @CacheEvict(value = "systemSettings", allEntries = true)
    public SystemSettingDto updateSettings(SystemSettingDto dto) {
        SystemSetting setting = repository.findById(1L).orElseGet(() -> new SystemSetting());
        
        setting.setAppointmentDuration(dto.getAppointmentDuration());
        setting.setWorkStartTime(dto.getWorkStartTime());
        setting.setWorkEndTime(dto.getWorkEndTime());
        setting.setLunchBreakStart(dto.getLunchBreakStart());
        setting.setLunchBreakEnd(dto.getLunchBreakEnd());
        setting.setMaintenanceMode(dto.getMaintenanceMode());

        setting = repository.save(setting);
        return mapToDto(setting);
    }

    private SystemSettingDto mapToDto(SystemSetting setting) {
        return new SystemSettingDto(
                setting.getAppointmentDuration(),
                setting.getWorkStartTime(),
                setting.getWorkEndTime(),
                setting.getLunchBreakStart(),
                setting.getLunchBreakEnd(),
                setting.getMaintenanceMode()
        );
    }
}
