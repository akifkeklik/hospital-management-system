package com.hospital.appointmentsystem.notification.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentsystem.notification.api.NotificationDto;
import com.hospital.appointmentsystem.notification.api.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatientNotifications_asAdmin_returnsOk() throws Exception {
        when(notificationService.getNotificationsByPatient(1L)).thenReturn(List.of(new NotificationDto(1L, 1L, null, null, "Hello", false, null)));

        mockMvc.perform(get("/api/notifications/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hello"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void broadcastToDoctors_asAdmin_returnsOk() throws Exception {
        mockMvc.perform(post("/api/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content("System Maintenance"))
                .andExpect(status().isOk());

        verify(notificationService).broadcastToDoctors("System Maintenance");
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void broadcastToDoctors_asPatient_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content("System Maintenance"))
                .andExpect(status().isForbidden());
    }

    // Notice: We don't test @securityService.isPatientOwner because that requires a full integration setup 
    // with custom security service beans properly configured for the mock user context. We just test role access.
}
