package com.hospital.appointmentsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.user.impl.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @org.springframework.boot.test.mock.mockito.MockBean
    private RateLimitFilter rateLimitFilter;

    @Value("${jwt.secret}")
    private String secretKeyString;

    private static final String UNIQUE_TC = "99887766554";
    private static final String UNIQUE_EMAIL = "newuser_auth_test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        // Pass mock filter through
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    // ==========================================
    // 1. REGISTER TESTS
    // ==========================================

    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", UNIQUE_TC);
        request.put("firstName", "Auth");
        request.put("lastName", "Test");
        request.put("email", UNIQUE_EMAIL);
        request.put("phoneNumber", "5550001122");
        request.put("password", "strongpassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Kullanıcı başarıyla kaydedildi."));

        // Verify patient and user were saved in DB
        assertTrue(userRepository.findByUsername(UNIQUE_TC).isPresent());
        
        // Ensure password is not stored in plain text
        String savedPassword = userRepository.findByUsername(UNIQUE_TC).get().getPassword();
        assertNotEquals("strongpassword", savedPassword);
        assertTrue(savedPassword.startsWith("$2a$")); // BCrypt prefix
    }

    @Test
    void shouldRejectDuplicateRegistration_TcId() throws Exception {
        // Pre-register
        shouldRegisterPatientSuccessfully();

        // Attempt duplicate TC
        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", UNIQUE_TC);
        request.put("firstName", "Duplicate");
        request.put("lastName", "Test2");
        request.put("email", "anotheremail@example.com");
        request.put("phoneNumber", "5550001133");
        request.put("password", "strongpassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hata: Bu TC Kimlik Numarası zaten kayıtlı."));
    }

    @Test
    void shouldRejectDuplicateRegistration_Email() throws Exception {
        shouldRegisterPatientSuccessfully();

        // Attempt duplicate Email
        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", "55566677788");
        request.put("firstName", "Duplicate");
        request.put("lastName", "Test2");
        request.put("email", UNIQUE_EMAIL);
        request.put("phoneNumber", "5550001133");
        request.put("password", "strongpassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hata: Bu e-posta adresi zaten kullanılıyor."));
    }

    @Test
    void shouldRejectShortPassword() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", "12345123450");
        request.put("firstName", "Short");
        request.put("lastName", "Pass");
        request.put("email", "shortpass@example.com");
        request.put("phoneNumber", "5550001144");
        request.put("password", "123"); // Less than 6 chars

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hata: Şifreniz en az 6 karakter olmalıdır."));
    }

    // ==========================================
    // 2. LOGIN TESTS & JWT GENERATION
    // ==========================================

    @Test
    void shouldLoginSuccessfullyAndReturnJwtCookie() throws Exception {
        shouldRegisterPatientSuccessfully();

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", UNIQUE_TC);
        loginRequest.put("password", "strongpassword");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsPasswordChange").value(false))
                .andReturn();

        // Verify HttpOnly cookie is set
        Cookie jwtCookie = result.getResponse().getCookie("jwt");
        assertNotNull(jwtCookie);
        assertTrue(jwtCookie.isHttpOnly());
        assertEquals("/", jwtCookie.getPath());
        assertNotNull(jwtCookie.getValue());
        
        // PII Check on token (password should not be in the plain response body or token)
        String responseBody = result.getResponse().getContentAsString();
        assertFalse(responseBody.contains("strongpassword"));
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        shouldRegisterPatientSuccessfully();

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", UNIQUE_TC);
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Giriş bilgileri hatalı."));
    }

    @Test
    void shouldRejectLoginForNonexistentUser() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "00000000000");
        loginRequest.put("password", "somepassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // Because authenticationManager.authenticate throws BadCredentialsException
                .andExpect(jsonPath("$.message").value("Giriş bilgileri hatalı."));
    }

    // ==========================================
    // 3. /ME ENDPOINT & ROLE MAPPING
    // ==========================================

    @Test
    void shouldGetMeDataSuccessfullyForPatient() throws Exception {
        shouldRegisterPatientSuccessfully();

        // Login to get token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", UNIQUE_TC);
        loginRequest.put("password", "strongpassword");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("jwt");

        // Access /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(UNIQUE_TC))
                .andExpect(jsonPath("$.role").value("ROLE_PATIENT"))
                .andExpect(jsonPath("$.firstName").value("Auth"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.email").value(UNIQUE_EMAIL));
    }

    // ==========================================
    // 4. EXPIRATION & INVALID TOKEN TESTS
    // ==========================================

    @Test
    void shouldDenyAccessWithExpiredJwt() throws Exception {
        // Manually create an expired token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_PATIENT");
        claims.put("referenceId", 1L);

        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setSubject("someuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(Keys.hmacShaKeyFor(secretKeyString.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/auth/me")
                .cookie(new Cookie("jwt", expiredToken)))
                .andExpect(status().isUnauthorized()); // Unauthenticated because filter skips expired token
    }

    @Test
    void shouldDenyAccessWithMalformedJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .cookie(new Cookie("jwt", "this.is.a.malformed.token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithMissingJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 5. PASSWORD RESET TESTS
    // ==========================================

    @Test
    void shouldHandleForgotPasswordRequest_ValidUser() throws Exception {
        shouldRegisterPatientSuccessfully();

        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", UNIQUE_TC);
        request.put("email", UNIQUE_EMAIL);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eğer bilgileriniz sistemimizde kayıtlıysa, şifre sıfırlama bağlantısı e-posta adresinize gönderilmiştir."));
    }

    @Test
    void shouldHandleForgotPasswordRequest_UnknownUser_SameResponse() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("tcIdentityNumber", "00000000000");
        request.put("email", "nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eğer bilgileriniz sistemimizde kayıtlıysa, şifre sıfırlama bağlantısı e-posta adresinize gönderilmiştir."));
    }

    @Test
    void shouldRejectResetPassword_InvalidToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "invalid_token_123");
        request.put("newPassword", "newstrongpassword");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hata: Geçersiz token."));
    }
}
