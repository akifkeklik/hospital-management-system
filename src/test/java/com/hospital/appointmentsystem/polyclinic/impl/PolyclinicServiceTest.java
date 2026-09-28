package com.hospital.appointmentsystem.polyclinic.impl;

import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolyclinicServiceTest {

    @Mock
    private PolyclinicRepository polyclinicRepository;

    @InjectMocks
    private PolyclinicServiceImpl polyclinicService;

    @Test
    void createPolyclinic_success() {
        PolyclinicDto dto = new PolyclinicDto();
        dto.setName("Dermatoloji");
        dto.setDepartmentId(1L);

        Polyclinic saved = new Polyclinic("Dermatoloji", "101", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 10L);
        
        when(polyclinicRepository.save(any(Polyclinic.class))).thenReturn(saved);

        PolyclinicDto result = polyclinicService.createPolyclinic(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Dermatoloji");
    }

    @Test
    void updatePolyclinic_notFound_throwsException() {
        PolyclinicDto dto = new PolyclinicDto();
        when(polyclinicRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polyclinicService.updatePolyclinic(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePolyclinic_success() {
        Polyclinic existing = new Polyclinic("Old", "101", 1L);
        when(polyclinicRepository.findById(1L)).thenReturn(Optional.of(existing));

        PolyclinicDto dto = new PolyclinicDto();
        dto.setName("New");
        dto.setDepartmentId(1L);

        when(polyclinicRepository.save(any(Polyclinic.class))).thenAnswer(i -> i.getArgument(0));

        PolyclinicDto result = polyclinicService.updatePolyclinic(1L, dto);

        assertThat(result.getName()).isEqualTo("New");
    }

    @Test
    void deletePolyclinic_success() {
        polyclinicService.deletePolyclinic(1L);
        verify(polyclinicRepository).deleteById(1L);
    }

    @Test
    void getAllPolyclinics_returnsList() {
        when(polyclinicRepository.findAll()).thenReturn(List.of(new Polyclinic("A", "1", 1L)));
        
        List<PolyclinicDto> result = polyclinicService.getAllPolyclinics();
        
        assertThat(result).hasSize(1);
    }

    @Test
    void getPolyclinicsByDepartment_returnsList() {
        when(polyclinicRepository.findByDepartmentId(1L)).thenReturn(List.of(new Polyclinic("A", "1", 1L)));
        
        List<PolyclinicDto> result = polyclinicService.getPolyclinicsByDepartment(1L);
        
        assertThat(result).hasSize(1);
    }
}
