package com.hospital.appointmentsystem.security;

import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.appointment.web.AppointmentController;
import com.hospital.appointmentsystem.department.web.DepartmentController;
import com.hospital.appointmentsystem.department.api.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AppointmentController.class, DepartmentController.class})
@Import(SecurityConfig.class) // Import SecurityConfig to apply real security filters
@AutoConfigureMockMvc(addFilters = true) // Ensure filters are active
public class SecurityEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter; // Mock custom filter

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // --- Mock the initData bean to prevent it from running and crashing during WebMvcTest ---
    @MockBean(name = "initData")
    private org.springframework.boot.CommandLineRunner initData;

    @MockBean(name = "auditorAware")
    private org.springframework.data.domain.AuditorAware<String> auditorAware;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;
    // -------------------------------------------------------------------------

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        // Ensure that mock filters pass the request down the chain
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // Mock service responses to prevent NPEs in Controller
        org.mockito.Mockito.when(appointmentService.getAllAppointments(org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        org.mockito.Mockito.when(appointmentService.getAppointmentsByDoctorId(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        org.mockito.Mockito.when(appointmentService.getAppointmentsByPatientId(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        org.mockito.Mockito.when(departmentService.getAllDepartments(org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
    }

    // ==========================================
    // 1. AUTHENTICATION TESTS (401)
    // ==========================================

    @Test
    void shouldReturn401_WhenAccessingProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. AUTHORIZATION TESTS (403 and 200)
    // ==========================================

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientCannotAccessAdminEndpoint_ShouldReturn403() throws Exception {
        // GET /api/appointments requires ADMIN role
        mockMvc.perform(get("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientCannotDeleteAppointment_ShouldReturn403() throws Exception {
        // DELETE /api/appointments/{id} requires ADMIN role
        mockMvc.perform(delete("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientCanAccessPatientEndpoint_ShouldReturn200() throws Exception {
        // GET /api/appointments/patient/{id} requires ADMIN, PATIENT, or DOCTOR
        mockMvc.perform(get("/api/appointments/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctorCanAccessDoctorEndpoint_ShouldReturn200() throws Exception {
        // GET /api/appointments/doctor/{id} requires ADMIN or DOCTOR
        mockMvc.perform(get("/api/appointments/doctor/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctorCannotAccessAdminEndpoint_ShouldReturn403() throws Exception {
        // GET /api/appointments requires ADMIN role
        mockMvc.perform(get("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAllEndpoints_ShouldReturn200() throws Exception {
        // Admin can access getAllAppointments
        mockMvc.perform(get("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Admin can access doctor appointments
        mockMvc.perform(get("/api/appointments/doctor/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 3. PUBLIC ENDPOINTS (200 without Auth)
    // ==========================================

    @Test
    void publicEndpointsShouldBeAccessibleWithoutToken() throws Exception {
        // GET /api/departments is public in SecurityConfig
        mockMvc.perform(get("/api/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
