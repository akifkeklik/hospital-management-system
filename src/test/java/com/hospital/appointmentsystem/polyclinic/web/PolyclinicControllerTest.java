package com.hospital.appointmentsystem.polyclinic.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicDto;
import com.hospital.appointmentsystem.polyclinic.api.PolyclinicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PolyclinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private PolyclinicService polyclinicService;

    @Test
    @WithMockUser(roles = "PATIENT") // Anyone authenticated can get
    void getAllPolyclinics_returnsOk() throws Exception {
        PolyclinicDto p = new PolyclinicDto();
        p.setId(1L);
        p.setName("Poly 1");
        when(polyclinicService.getAllPolyclinics()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/admin/polyclinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Poly 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPolyclinic_asAdmin_returnsOk() throws Exception {
        PolyclinicDto req = new PolyclinicDto();
        req.setName("Poly 2");
        
        PolyclinicDto res = new PolyclinicDto();
        res.setId(2L);
        res.setName("Poly 2");
        when(polyclinicService.createPolyclinic(any())).thenReturn(res);

        mockMvc.perform(post("/api/admin/polyclinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void createPolyclinic_asDoctor_returnsForbidden() throws Exception {
        PolyclinicDto req = new PolyclinicDto();
        req.setName("Poly 3");

        mockMvc.perform(post("/api/admin/polyclinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePolyclinic_asAdmin_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/admin/polyclinics/1"))
                .andExpect(status().isOk());

        verify(polyclinicService).deletePolyclinic(1L);
    }
}
