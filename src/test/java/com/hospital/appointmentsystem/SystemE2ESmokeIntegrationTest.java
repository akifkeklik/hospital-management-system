package com.hospital.appointmentsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequest;
import com.hospital.appointmentsystem.doctor.impl.DoctorRegistrationRequestRepository;
import com.hospital.appointmentsystem.security.AuthController;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.test.context.ActiveProfiles;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "JWT_SECRET=my_super_secret_key_for_testing_purposes_only",
        "ADMIN_PASSWORD=dev_admin_secret_key_2026",
        "TEST_USER_PASSWORD=local_test_secret_2026",
        "rate-limit.auth.capacity=1000"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class SystemE2ESmokeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRegistrationRequestRepository doctorRegistrationRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.password:dev_admin_secret_key_2026}")
    private String adminPassword;

    @Value("${test.user.password:local_test_secret_2026}")
    private String testUserPassword;

    private Cookie adminCookie;
    private Cookie patientCookie;
    private Cookie doctorCookie;

    @BeforeEach
    void setup() throws Exception {
        // 1. Admin Login
        AuthController.AuthRequest adminReq = new AuthController.AuthRequest("admin", adminPassword);
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isOk())
                .andReturn();
        adminCookie = adminResult.getResponse().getCookie("jwt");

        // 2. Patient Login (Created by seeder: 99999999999)
        AuthController.AuthRequest patientReq = new AuthController.AuthRequest("99999999999", testUserPassword);
        MvcResult patientResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientReq)))
                .andExpect(status().isOk())
                .andReturn();
        patientCookie = patientResult.getResponse().getCookie("jwt");

        // 3. Doctor Login (Created by seeder: 88888888888)
        AuthController.AuthRequest doctorReq = new AuthController.AuthRequest("88888888888", testUserPassword);
        MvcResult doctorResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorReq)))
                .andExpect(status().isOk())
                .andReturn();
        doctorCookie = doctorResult.getResponse().getCookie("jwt");
    }

    @Test
    void testAuthE2E() throws Exception {
        mockMvc.perform(get("/api/auth/me").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout").cookie(adminCookie))
                .andExpect(status().isOk())
                .andReturn();
        Cookie deletedCookie = logoutResult.getResponse().getCookie("jwt");

        mockMvc.perform(get("/api/auth/me").cookie(deletedCookie))
                .andExpect(status().isUnauthorized());

        AuthController.DoctorRegisterRequest doctorReg = new AuthController.DoctorRegisterRequest(
                "33333333333", "New", "Doctor", "newdoc@hospital.com", "05553333333", 1L, "Uzman"
        );
        mockMvc.perform(post("/api/auth/doctor-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorReg)))
                .andExpect(status().isOk());

        List<DoctorRegistrationRequest> requests = doctorRegistrationRequestRepository.findAll();
        Long requestId = requests.stream().filter(r -> "33333333333".equals(r.getTcIdentityNumber())).findFirst().get().getId();

        mockMvc.perform(post("/api/admin/doctor-requests/" + requestId + "/approve").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(not(containsString("DR-"))));

        User newDocUser = userRepository.findByUsername("33333333333").orElseThrow();
        newDocUser.setPassword(passwordEncoder.encode("temp123"));
        userRepository.save(newDocUser);

        AuthController.AuthRequest newDocReq = new AuthController.AuthRequest("33333333333", "temp123");
        MvcResult newDocLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDocReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsPasswordChange").value(true))
                .andReturn();
        Cookie newDocCookie = newDocLoginResult.getResponse().getCookie("jwt");

        AuthController.ForceChangePasswordRequest wrongForceReq = new AuthController.ForceChangePasswordRequest("33333333333", "wrong", "newpass123");
        mockMvc.perform(post("/api/auth/force-change-password")
                .cookie(newDocCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongForceReq)))
                .andExpect(status().isUnauthorized());

        AuthController.ForceChangePasswordRequest forceReq = new AuthController.ForceChangePasswordRequest("33333333333", "temp123", "newpass123");
        mockMvc.perform(post("/api/auth/force-change-password")
                .cookie(newDocCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forceReq)))
                .andExpect(status().isOk());
    }

    @Test
    void testAuthorizationAndIDOR() throws Exception {
        mockMvc.perform(get("/api/admin/doctor-requests").cookie(patientCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/doctor-requests").cookie(doctorCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk());
    }

    @Test
    void testAppointmentFlowAndValidation() throws Exception {
        String myDocJson = mockMvc.perform(get("/api/auth/me").cookie(doctorCookie))
                .andReturn().getResponse().getContentAsString();
        Long doctorId = objectMapper.readTree(myDocJson).get("id").asLong();

        String myPatJson = mockMvc.perform(get("/api/auth/me").cookie(patientCookie))
                .andReturn().getResponse().getContentAsString();
        Long patientId = objectMapper.readTree(myPatJson).get("id").asLong();

        mockMvc.perform(get("/api/appointments/available-slots")
                .param("doctorId", doctorId.toString())
                .param("date", LocalDate.now().plusDays(2).toString())
                .cookie(patientCookie))
                .andExpect(status().isOk());

        AppointmentDto appt = new AppointmentDto();
        appt.setDoctorId(doctorId);
        appt.setPatientId(patientId);
        appt.setAppointmentDate(LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(10, 0)));
        appt.setNotes("Test appointment");

        MvcResult apptResult = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appt))
                .cookie(patientCookie))
                .andExpect(status().is(isOneOf(200, 201)))
                .andReturn();
        
        Long apptId = objectMapper.readTree(apptResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/appointments/" + apptId).cookie(patientCookie))
                .andExpect(status().isOk());
                
        // Test IDOR: Patient trying to delete appointment created by them (Wait, patient can't delete in some systems, let's just GET)
        
        // Let's create a new patient and try to access
        AuthController.RegisterRequest otherPat = new AuthController.RegisterRequest(
                "44444444444", "Other", "Patient", "other@patient.com", "05554444444", "password123"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherPat)))
                .andExpect(status().isOk());
                
        AuthController.AuthRequest otherPatReq = new AuthController.AuthRequest("44444444444", "password123");
        MvcResult otherPatResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherPatReq)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie otherPatCookie = otherPatResult.getResponse().getCookie("jwt");
        
        // Other patient tries to get the appointment
        mockMvc.perform(get("/api/appointments/" + apptId).cookie(otherPatCookie))
                .andExpect(status().isForbidden());
                
        // Validation tests
        AppointmentDto invalidAppt = new AppointmentDto();
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAppt))
                .cookie(patientCookie))
                .andExpect(status().isBadRequest());
    }
}
