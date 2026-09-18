package com.hospital.appointmentsystem.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.patient.api.PatientService;
import com.hospital.appointmentsystem.patient.web.PatientRequest;
import com.hospital.appointmentsystem.security.RateLimitFilter;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RateLimitFilter rateLimitFilter;

    @org.springframework.boot.test.mock.mockito.SpyBean
    private PatientService patientService;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        User admin = new User();
        admin.setUsername("ADMIN");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN");
        admin.setEmail("admin_geh@test.com");
        userRepository.save(admin);

        User patient = new User();
        patient.setUsername("PATIENT");
        patient.setPassword(passwordEncoder.encode("password"));
        patient.setRole("ROLE_PATIENT");
        patient.setEmail("patient_geh@test.com");
        patient.setReferenceId(1L);
        userRepository.save(patient);
    }

    private Cookie loginAndGetCookie(String username) throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", username);
        loginReq.put("password", "password");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("jwt");
    }

    @Test
    void shouldReturn400_whenValidationFails() throws Exception {
        Cookie adminCookie = loginAndGetCookie("ADMIN");

        // Request with null fields that should be @NotBlank
        PatientRequest req = new PatientRequest();
        
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .cookie(adminCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/patients"))
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn401_whenNotAuthenticated() throws Exception {
        // We will call a protected endpoint without any cookie
        // Note: Spring Security intercepts this and returns 401 via AuthenticationEntryPoint, not GlobalExceptionHandler
        // If GlobalExceptionHandler doesn't handle it, we will just assert status 401. 
        // We do NOT assert jsonPath for status because it might be empty if handled by basic EntryPoint.
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403_whenAuthenticatedButUnauthorized() throws Exception {
        Cookie patientCookie = loginAndGetCookie("PATIENT");

        mockMvc.perform(get("/api/stats/dashboard")
                .cookie(patientCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/stats/dashboard"));
    }

    @Test
    void shouldReturn404_whenResourceNotFound() throws Exception {
        Cookie adminCookie = loginAndGetCookie("ADMIN");

        // Force a ResourceNotFoundException from service
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Patient", "id", 999999L))
                .when(patientService).getPatientById(999999L);

        mockMvc.perform(get("/api/patients/999999")
                .cookie(adminCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/patients/999999"));
    }

    @Test
    void shouldReturn409_whenDataIntegrityViolation() throws Exception {
        Cookie adminCookie = loginAndGetCookie("ADMIN");

        // Mock DB constraint exception
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("duplicate key value"))
                .when(patientService).createPatient(org.mockito.ArgumentMatchers.any());

        PatientRequest req = new PatientRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setTcIdentityNumber("11111111111");
        req.setPhoneNumber("5555555555");
        req.setEmail("test@test.com");

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .cookie(adminCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Mükerrer TC")))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/patients"));
    }

    @Test
    void shouldReturn500_andNotDiscloseSensitiveInfo_whenUnexpectedErrorOccurs() throws Exception {
        Cookie adminCookie = loginAndGetCookie("ADMIN");

        org.mockito.Mockito.doThrow(new RuntimeException("SECRET_INTERNAL_DB_PASSWORD_123"))
                .when(patientService).getAllPatients(org.mockito.ArgumentMatchers.any());

        MvcResult result = mockMvc.perform(get("/api/patients")
                .cookie(adminCookie))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/patients"))
                .andReturn();
                
        String responseBody = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertFalse(responseBody.contains("SECRET_INTERNAL_DB_PASSWORD_123"), "Sensitive internal message leaked!");
        org.junit.jupiter.api.Assertions.assertFalse(responseBody.contains("RuntimeException"), "Exception class leaked!");
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("Beklenmeyen bir hata oluştu"), "Generic safe message not found");
    }
}
