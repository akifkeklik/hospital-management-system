package com.hospital.appointmentsystem.stats;

import com.hospital.appointmentsystem.stats.dto.DashboardStatsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardStatsService dashboardStatsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getDashboardStats_asAdmin_shouldReturnStats() throws Exception {
        DashboardStatsDTO mockStats = new DashboardStatsDTO();
        mockStats.setTotalDepartments(5);
        mockStats.setTotalDoctors(10);
        mockStats.setTotalPatients(20);
        mockStats.setTotalAppointments(30);

        HashMap<String, Long> docDist = new HashMap<>();
        docDist.put("Kardiyoloji", 5L);
        mockStats.setDoctorDistribution(docDist);

        HashMap<String, Long> apptsByDate = new HashMap<>();
        apptsByDate.put("2026-09-12", 30L);
        mockStats.setAppointmentsByDate(apptsByDate);

        when(dashboardStatsService.getDashboardStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/stats/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDepartments").value(5))
                .andExpect(jsonPath("$.totalDoctors").value(10))
                .andExpect(jsonPath("$.totalPatients").value(20))
                .andExpect(jsonPath("$.totalAppointments").value(30))
                .andExpect(jsonPath("$.doctorDistribution.Kardiyoloji").value(5))
                .andExpect(jsonPath("$.appointmentsByDate.['2026-09-12']").value(30));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    public void getDashboardStats_asPatient_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/stats/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "DOCTOR")
    public void getDashboardStats_asDoctor_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/stats/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getDashboardStats_asUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/stats/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Or isForbidden depending on security config
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getDashboardStats_withEmptyData_shouldReturnZeros() throws Exception {
        DashboardStatsDTO mockStats = new DashboardStatsDTO();
        mockStats.setTotalDepartments(0);
        mockStats.setTotalDoctors(0);
        mockStats.setTotalPatients(0);
        mockStats.setTotalAppointments(0);
        mockStats.setDoctorDistribution(new HashMap<>());
        mockStats.setAppointmentsByDate(new HashMap<>());

        when(dashboardStatsService.getDashboardStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/stats/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDepartments").value(0))
                .andExpect(jsonPath("$.totalDoctors").value(0))
                .andExpect(jsonPath("$.totalPatients").value(0))
                .andExpect(jsonPath("$.totalAppointments").value(0));
    }
}
