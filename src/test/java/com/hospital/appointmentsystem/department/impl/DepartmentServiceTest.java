package com.hospital.appointmentsystem.department.impl;

import com.hospital.appointmentsystem.department.api.DepartmentDto;
import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void createDepartment_success() {
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Cardiology");

        when(departmentRepository.existsByName("Cardiology")).thenReturn(false);
        Department saved = new Department();
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 1L);
        saved.setName("Cardiology");
        
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        DepartmentDto result = departmentService.createDepartment(dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createDepartment_duplicateName_throwsException() {
        DepartmentDto dto = new DepartmentDto();
        dto.setName("Cardiology");

        when(departmentRepository.existsByName("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(dto))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getDepartmentById_notFound_throwsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDepartment_notFound_throwsException() {
        DepartmentDto dto = new DepartmentDto();
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDepartment_success() {
        Department existing = new Department();
        existing.setName("Old");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        DepartmentDto dto = new DepartmentDto();
        dto.setName("New");
        
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        DepartmentDto result = departmentService.updateDepartment(1L, dto);

        assertThat(result.getName()).isEqualTo("New");
    }

    @Test
    void deleteDepartment_notFound_throwsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteDepartment_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(new Department()));

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void searchDepartments_returnsPage() {
        Department d = new Department();
        d.setName("Search Dept");
        when(departmentRepository.searchDepartments(eq("Search"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(d)));

        Page<DepartmentDto> result = departmentService.searchDepartments("Search", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }
}
