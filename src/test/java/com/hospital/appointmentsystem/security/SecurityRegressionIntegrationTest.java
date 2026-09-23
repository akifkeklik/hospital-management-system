package com.hospital.appointmentsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequest;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequestRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "JWT_SECRET=test_dummy_secret_key_that_is_at_least_32_characters_long",
        "ADMIN_PASSWORD=test_admin_password",
        "TEST_USER_PASSWORD=local_test_secret_2026",
        "rate-limit.auth.capacity=1000"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SecurityRegressionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRegistrationRequestRepository registrationRequestRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${TEST_USER_PASSWORD}")
    private String testUserPassword;

    private Cookie adminCookie;
    private Cookie patientCookie;
    private Cookie doctorCookie;

    @BeforeEach
    void setUp() throws Exception {
        // Authenticate admin
        Map<String, String> adminCreds = new HashMap<>();
        adminCreds.put("username", "admin");
        adminCreds.put("password", "test_admin_password");
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminCreds)))
                .andExpect(status().isOk())
                .andReturn();
        adminCookie = adminRes.getResponse().getCookie("jwt");

        // Authenticate patient (99999999999 is seeded)
        Map<String, String> patCreds = new HashMap<>();
        patCreds.put("username", "99999999999");
        patCreds.put("password", testUserPassword);
        MvcResult patRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patCreds)))
                .andExpect(status().isOk())
                .andReturn();
        patientCookie = patRes.getResponse().getCookie("jwt");

        // Authenticate doctor (88888888888 is seeded)
        Map<String, String> docCreds = new HashMap<>();
        docCreds.put("username", "88888888888");
        docCreds.put("password", testUserPassword);
        MvcResult docRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(docCreds)))
                .andExpect(status().isOk())
                .andReturn();
        doctorCookie = docRes.getResponse().getCookie("jwt");
    }

    // 1. unauthenticated protected endpoint -> 401
    @Test
    void unauthenticatedProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // 2. invalid JWT -> 401
    @Test
    void invalidJwt() throws Exception {
        Cookie invalidCookie = new Cookie("jwt", "this.is.an.invalid.token");
        mockMvc.perform(get("/api/auth/me").cookie(invalidCookie))
                .andExpect(status().isUnauthorized());
    }

    // 3. expired JWT -> 401
    @Test
    void expiredJwt() throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_PATIENT");
        claims.put("referenceId", 1L);
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setSubject("99999999999")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Past
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        Cookie expiredCookie = new Cookie("jwt", expiredToken);
        mockMvc.perform(get("/api/auth/me").cookie(expiredCookie))
                .andExpect(status().isUnauthorized());
    }

    // 4. patient -> admin endpoint -> 403
    @Test
    void patientToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/doctor-requests").cookie(patientCookie))
                .andExpect(status().isForbidden());
    }

    // 5. doctor -> admin endpoint -> 403
    @Test
    void doctorToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/doctor-requests").cookie(doctorCookie))
                .andExpect(status().isForbidden());
    }

    // 6. patient -> another patient's resource -> 403
    @Test
    void patientToAnotherPatientResource() throws Exception {
        mockMvc.perform(get("/api/patients/2").cookie(patientCookie))
                .andExpect(status().isForbidden()); 
    }

    // 7. doctor -> unauthorized resource -> 403
    @Test
    void doctorToUnauthorizedResource() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("name", "Test Dept");
        req.put("description", "Test");
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .cookie(doctorCookie))
                .andExpect(status().isForbidden());
    }

    // 8. public GET /api/doctors -> 200
    @Test
    void publicGetDoctors() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk());
    }

    // 9. public GET /api/departments -> 200
    @Test
    void publicGetDepartments() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }

    // 10. public POST /api/doctors -> denied
    @Test
    void publicPostDoctorsDenied() throws Exception {
        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized()); 
    }

    // 11. reset-password with invalid token -> 400 Bad Request with invalid token message
    @Test
    void resetPasswordInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"invalid_or_nonexistent_token\", \"newPassword\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Geçersiz token")));
    }

    // 12. wrong oldPassword -> denied
    @Test
    void wrongOldPassword() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("tcIdentityNumber", "99999999999");
        req.put("oldPassword", "WRONG_PASSWORD");
        req.put("newPassword", "newSecurePassword");

        mockMvc.perform(post("/api/auth/force-change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .cookie(patientCookie))
                .andExpect(status().isBadRequest()); 
    }

    // 13 & 14 & 15. temporary password leak, JWT leak -> absent
    @Test
    void doctorRegistrationRequestPasswordNotLeaked() throws Exception {
        DoctorRegistrationRequest req = new DoctorRegistrationRequest();
        req.setTcIdentityNumber("12312312312");
        req.setFirstName("Leak");
        req.setLastName("Test");
        req.setEmail("leak@test.com");
        req.setPhoneNumber("123");
        req.setDepartmentId(1L);
        req.setPassword("SUPER_SECRET_LEAK");
        req.setStatus("PENDING");
        req.setRequestDate(LocalDateTime.now());
        registrationRequestRepository.save(req);

        MvcResult res = mockMvc.perform(get("/api/admin/doctor-requests").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString();

        assertFalse(json.contains("SUPER_SECRET_LEAK"), "Password should NOT leak in response");
        assertFalse(json.contains("password\""), "Password field should NOT exist in JSON");
    }

    // 16 & 17. error response -> no stack trace, no SQL
    @Test
    void errorResponseStructure() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\": 999, \"doctorId\": 999}")
                .cookie(patientCookie))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String json = res.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("validationErrors"));
        assertFalse(json.contains("java.lang"), "Should not contain stack traces");
        assertFalse(json.contains("sql"), "Should not contain SQL");
    }

    // 18. logout -> authentication removed
    @Test
    void logoutTest() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/logout").cookie(patientCookie))
                .andExpect(status().isOk())
                .andReturn();
        
        Cookie cookie = res.getResponse().getCookie("jwt");
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge(), "Cookie should be expired");
    }
}
