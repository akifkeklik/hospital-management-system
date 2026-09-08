package com.hospital.appointmentsystem.security;

import com.hospital.appointmentsystem.patient.api.PatientDto;
import com.hospital.appointmentsystem.patient.api.PatientService;
import com.hospital.appointmentsystem.patient.web.PatientController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 🔒 Patient Endpoint Security Tests — Patient Authorization Hardening
 *
 * Validates that:
 * - DOCTOR cannot access GET /api/patients or GET /api/patients/{id}
 * - PATIENT can only access their own profile via isPatientOwner
 * - ADMIN retains full access
 * - Unauthenticated requests get 401
 * - IDOR scenarios are blocked (DOCTOR→PATIENT_B, PATIENT_A→PATIENT_B)
 */
@WebMvcTest(controllers = {PatientController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = true)
public class PatientEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean(name = "securityService")
    private SecurityService securityService;

    @MockBean(name = "initData")
    private org.springframework.boot.CommandLineRunner initData;

    @MockBean(name = "auditorAware")
    private org.springframework.data.domain.AuditorAware<String> auditorAware;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @BeforeEach
    void setUp() throws Exception {
        // Pass mock filters through
        Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        // Mock service responses
        Mockito.when(patientService.getAllPatients(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Page.empty());
        Mockito.when(patientService.getPatientById(anyLong()))
                .thenReturn(new PatientDto());

        // Default SecurityService mocks — ownership denied
        Mockito.when(securityService.isPatientOwner(anyLong())).thenReturn(false);
        Mockito.when(securityService.isDoctorOwner(anyLong())).thenReturn(false);
    }

    // ==========================================
    // GET /api/patients — ALL PATIENTS LIST
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canListAllPatients_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctor_cannotListAllPatients_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patient_cannotListAllPatients_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_cannotListAllPatients_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // GET /api/patients/{id} — SINGLE PATIENT
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canGetPatientById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientOwner_canGetOwnProfile_shouldReturn200() throws Exception {
        // Simulate: this patient IS the owner of patient ID 1
        Mockito.when(securityService.isPatientOwner(1L)).thenReturn(true);

        mockMvc.perform(get("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patient_cannotGetOtherPatientProfile_shouldReturn403() throws Exception {
        // Simulate: this patient is NOT the owner of patient ID 99
        Mockito.when(securityService.isPatientOwner(99L)).thenReturn(false);

        mockMvc.perform(get("/api/patients/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctor_cannotGetAnyPatientProfile_shouldReturn403() throws Exception {
        // IDOR: DOCTOR_A → GET /api/patients/PATIENT_B = 403
        mockMvc.perform(get("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_cannotGetPatientById_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // IDOR VERIFICATION SCENARIOS
    // ==========================================

    @Test
    @WithMockUser(roles = "DOCTOR")
    void idor_doctorAccessingArbitraryPatient_shouldReturn403() throws Exception {
        // DOCTOR_A → GET /api/patients/PATIENT_B (no appointment relationship)
        mockMvc.perform(get("/api/patients/42")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void idor_patientAccessingOtherPatient_shouldReturn403() throws Exception {
        // PATIENT_A → GET /api/patients/PATIENT_B
        Mockito.when(securityService.isPatientOwner(77L)).thenReturn(false);

        mockMvc.perform(get("/api/patients/77")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
