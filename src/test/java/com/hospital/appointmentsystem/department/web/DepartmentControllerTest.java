package com.hospital.appointmentsystem.department.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.department.api.DepartmentDto;
import com.hospital.appointmentsystem.department.api.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private DepartmentService departmentService;

    @Test
    @WithMockUser(roles = "PATIENT")
    void getAllDepartments_returnsOk() throws Exception {
        DepartmentDto d = new DepartmentDto();
        d.setId(1L);
        d.setName("Dept 1");
        when(departmentService.getAllDepartments(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(d)));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Dept 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepartment_asAdmin_returnsCreated() throws Exception {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("Dept 2");
        req.setDescription("Desc");
        
        DepartmentDto res = new DepartmentDto();
        res.setId(2L);
        res.setName("Dept 2");
        when(departmentService.createDepartment(any())).thenReturn(res);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void createDepartment_asDoctor_returnsForbidden() throws Exception {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("Dept 3");
        req.setDescription("Desc");

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDepartment_asAdmin_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());

        verify(departmentService).deleteDepartment(1L);
    }
}
