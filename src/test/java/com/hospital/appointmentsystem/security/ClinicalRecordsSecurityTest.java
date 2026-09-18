package com.hospital.appointmentsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.appointment.impl.Appointment;
import com.hospital.appointmentsystem.appointment.impl.AppointmentRepository;
import com.hospital.appointmentsystem.appointment.impl.AppointmentStatus;
import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.examination.api.DiagnosisDto;
import com.hospital.appointmentsystem.examination.api.PrescriptionDto;
import com.hospital.appointmentsystem.examination.impl.Diagnosis;
import com.hospital.appointmentsystem.examination.impl.DiagnosisRepository;
import com.hospital.appointmentsystem.examination.impl.Prescription;
import com.hospital.appointmentsystem.examination.impl.PrescriptionRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import com.hospital.appointmentsystem.user.impl.User;
import com.hospital.appointmentsystem.user.impl.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ClinicalRecordsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RateLimitFilter rateLimitFilter;

    private Long patientAId;
    private Long patientBId;
    private Long doctorAId;
    private Long doctorBId;
    
    private Long appointmentAId; // Doctor A -> Patient A
    private Long appointmentBId; // Doctor B -> Patient B
    
    private Long diagnosisAId;
    private Long prescriptionAId;

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

        // Create Department
        Department dept = new Department();
        dept.setName("TestDept");
        dept = departmentRepository.save(dept);

        // Create Doctors
        Doctor docA = new Doctor();
        docA.setFirstName("Doctor");
        docA.setLastName("A");
        docA.setTcIdentityNumber("DOC_A");
        docA.setSpecialization("General");
        docA.setEmail("doca@test.com");
        docA.setPhoneNumber("5551112233");
        docA.setDepartment(dept);
        docA = doctorRepository.save(docA);
        doctorAId = docA.getId();

        Doctor docB = new Doctor();
        docB.setFirstName("Doctor");
        docB.setLastName("B");
        docB.setTcIdentityNumber("DOC_B");
        docB.setSpecialization("General");
        docB.setEmail("docb@test.com");
        docB.setPhoneNumber("5551112244");
        docB.setDepartment(dept);
        docB = doctorRepository.save(docB);
        doctorBId = docB.getId();

        // Create Patients
        Patient patA = new Patient();
        patA.setFirstName("Patient");
        patA.setLastName("A");
        patA.setTcIdentityNumber("PAT_A");
        patA.setEmail("pata@test.com");
        patA.setPhoneNumber("5551112255");
        patA = patientRepository.save(patA);
        patientAId = patA.getId();

        Patient patB = new Patient();
        patB.setFirstName("Patient");
        patB.setLastName("B");
        patB.setTcIdentityNumber("PAT_B");
        patB.setEmail("patb@test.com");
        patB.setPhoneNumber("5551112266");
        patB = patientRepository.save(patB);
        patientBId = patB.getId();

        // Create Users for Auth
        User userDocA = new User();
        userDocA.setUsername("DOC_A");
        userDocA.setPassword(passwordEncoder.encode("password"));
        userDocA.setRole("ROLE_DOCTOR");
        userDocA.setReferenceId(doctorAId);
        userDocA.setEmail("doca@test.com");
        userRepository.save(userDocA);

        User userDocB = new User();
        userDocB.setUsername("DOC_B");
        userDocB.setPassword(passwordEncoder.encode("password"));
        userDocB.setRole("ROLE_DOCTOR");
        userDocB.setReferenceId(doctorBId);
        userDocB.setEmail("docb@test.com");
        userRepository.save(userDocB);

        User userPatA = new User();
        userPatA.setUsername("PAT_A");
        userPatA.setPassword(passwordEncoder.encode("password"));
        userPatA.setRole("ROLE_PATIENT");
        userPatA.setReferenceId(patientAId);
        userPatA.setEmail("pata@test.com");
        userRepository.save(userPatA);

        User userPatB = new User();
        userPatB.setUsername("PAT_B");
        userPatB.setPassword(passwordEncoder.encode("password"));
        userPatB.setRole("ROLE_PATIENT");
        userPatB.setReferenceId(patientBId);
        userPatB.setEmail("patb@test.com");
        userRepository.save(userPatB);

        // Create Appointments
        Appointment appA = new Appointment();
        appA.setDoctor(docA);
        appA.setPatient(patA);
        appA.setStatus(AppointmentStatus.COMPLETED);
        appA.setAppointmentDate(LocalDateTime.now().minusDays(1));
        appA = appointmentRepository.save(appA);
        appointmentAId = appA.getId();

        Appointment appB = new Appointment();
        appB.setDoctor(docB);
        appB.setPatient(patB);
        appB.setStatus(AppointmentStatus.COMPLETED);
        appB.setAppointmentDate(LocalDateTime.now().minusDays(1));
        appB = appointmentRepository.save(appB);
        appointmentBId = appB.getId();

        // Create Records for Appointment A
        Diagnosis diagA = new Diagnosis();
        diagA.setAppointment(appA);
        diagA.setDescription("Flu");
        diagA.setIcd10Code("J10");
        diagA = diagnosisRepository.save(diagA);
        diagnosisAId = diagA.getId();

        Prescription prescA = new Prescription();
        prescA.setAppointment(appA);
        prescA.setMedicationName("Paracetamol");
        prescA.setDosage("1x1");
        prescA.setUsageInstruction("oral");
        prescA = prescriptionRepository.save(prescA);
        prescriptionAId = prescA.getId();
    }

    private Cookie loginAndGetCookie(String username, String password) throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("jwt");
    }

    // ==========================================
    // PATIENT IDOR TESTS
    // ==========================================

    @Test
    void patientA_canReadOwnRecords_shouldReturn200() throws Exception {
        Cookie patientACookie = loginAndGetCookie("PAT_A", "password");

        mockMvc.perform(get("/api/examinations/appointments/" + appointmentAId + "/prescriptions")
                .cookie(patientACookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/examinations/appointments/" + appointmentAId + "/diagnoses")
                .cookie(patientACookie))
                .andExpect(status().isOk());
    }

    @Test
    void patientA_cannotReadPatientBRecords_shouldReturn403() throws Exception {
        Cookie patientACookie = loginAndGetCookie("PAT_A", "password");

        // Attempt to read Patient B's appointment prescriptions
        mockMvc.perform(get("/api/examinations/appointments/" + appointmentBId + "/prescriptions")
                .cookie(patientACookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/examinations/appointments/" + appointmentBId + "/diagnoses")
                .cookie(patientACookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientA_cannotDeleteOwnPrescription_shouldReturn403() throws Exception {
        Cookie patientACookie = loginAndGetCookie("PAT_A", "password");

        mockMvc.perform(delete("/api/examinations/prescriptions/" + prescriptionAId)
                .cookie(patientACookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientA_cannotCreateDiagnosis_shouldReturn403() throws Exception {
        Cookie patientACookie = loginAndGetCookie("PAT_A", "password");
        
        DiagnosisDto request = new DiagnosisDto();
        request.setAppointmentId(appointmentAId);
        request.setDescription("Test");
        request.setIcd10Code("X99");

        mockMvc.perform(post("/api/examinations/diagnoses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(patientACookie))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // DOCTOR IDOR TESTS
    // ==========================================

    @Test
    void doctorA_canReadPatientARecords_shouldReturn200() throws Exception {
        Cookie doctorACookie = loginAndGetCookie("DOC_A", "password");

        mockMvc.perform(get("/api/examinations/appointments/" + appointmentAId + "/prescriptions")
                .cookie(doctorACookie))
                .andExpect(status().isOk());
    }

    @Test
    void doctorA_cannotReadPatientBRecords_shouldReturn403() throws Exception {
        Cookie doctorACookie = loginAndGetCookie("DOC_A", "password");

        // Doctor A tries to access Doctor B's appointment (Patient B)
        mockMvc.perform(get("/api/examinations/appointments/" + appointmentBId + "/prescriptions")
                .cookie(doctorACookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorA_canCreatePrescriptionForPatientA_shouldReturn200() throws Exception {
        Cookie doctorACookie = loginAndGetCookie("DOC_A", "password");
        
        PrescriptionDto dto = new PrescriptionDto();
        dto.setAppointmentId(appointmentAId);
        dto.setMedicationName("Aspirin");
        dto.setDosage("1x1");
        dto.setUsageInstruction("oral");

        mockMvc.perform(post("/api/examinations/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .cookie(doctorACookie))
                .andExpect(status().isOk());
    }

    @Test
    void doctorA_cannotCreatePrescriptionForPatientB_shouldReturn403() throws Exception {
        Cookie doctorACookie = loginAndGetCookie("DOC_A", "password");
        
        // Doctor A tries to prescribe for Doctor B's appointment
        PrescriptionDto dto = new PrescriptionDto();
        dto.setAppointmentId(appointmentBId);
        dto.setMedicationName("Aspirin");
        dto.setDosage("1x1");
        dto.setUsageInstruction("oral");

        mockMvc.perform(post("/api/examinations/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .cookie(doctorACookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorB_cannotDeleteDoctorAPrescription_shouldReturn403() throws Exception {
        Cookie doctorBCookie = loginAndGetCookie("DOC_B", "password");

        // Doctor B tries to delete Doctor A's prescription
        mockMvc.perform(delete("/api/examinations/prescriptions/" + prescriptionAId)
                .cookie(doctorBCookie))
                .andExpect(status().isForbidden());
    }
}
