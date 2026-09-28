package com.hospital.appointmentsystem.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Web tests for {@link AiController}.
 *
 * <p>Uses Spring's MockMvc to test the HTTP endpoint without starting a full server.
 * {@link AiService} is mocked to isolate controller logic.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Valid request parsing and response generation</li>
 *   <li>Authentication (the endpoint should allow access)</li>
 *   <li>Error handling from service</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @Test
    @DisplayName("POST /api/ai/analyze-symptoms - Success")
    @WithMockUser(username = "testuser")
    void analyzeSymptoms_success() throws Exception {
        // Arrange
        AiDtos.SymptomAnalysisRequest request = new AiDtos.SymptomAnalysisRequest();
        request.setSymptoms("baş ağrısı");
        request.setAvailableDepartments(List.of("Nöroloji", "Dahiliye"));

        AiDtos.SymptomAnalysisResponse mockResponse = new AiDtos.SymptomAnalysisResponse(
                "Nöroloji", 85, "Nörolojik muayene önerilir.", "Uyarı"
        );

        when(aiService.analyzeSymptoms(eq("baş ağrısı"), anyList(), eq("testuser")))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/ai/analyze-symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedDepartment").value("Nöroloji"))
                .andExpect(jsonPath("$.confidencePercent").value(85))
                .andExpect(jsonPath("$.explanation").value("Nörolojik muayene önerilir."))
                .andExpect(jsonPath("$.disclaimer").value("Uyarı"));
    }

    @Test
    @DisplayName("POST /api/ai/analyze-symptoms - Anonymous user returns 401")
    void analyzeSymptoms_anonymous_returnsUnauthorized() throws Exception {
        // Arrange
        AiDtos.SymptomAnalysisRequest request = new AiDtos.SymptomAnalysisRequest();
        request.setSymptoms("mide bulantısı");
        request.setAvailableDepartments(List.of("Dahiliye"));

        // Act & Assert
        mockMvc.perform(post("/api/ai/analyze-symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // SecurityConfig blocks /api/** for anonymous
    }

    @Test
    @DisplayName("POST /api/ai/analyze-symptoms - Empty body / Bad Request")
    @WithMockUser
    void analyzeSymptoms_emptyBody_badRequest() throws Exception {
        // Missing required body
        mockMvc.perform(post("/api/ai/analyze-symptoms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/ai/analyze-symptoms - Service returns error response")
    @WithMockUser
    void analyzeSymptoms_serviceError_returnsOkWithErrorContent() throws Exception {
        // Arrange
        AiDtos.SymptomAnalysisRequest request = new AiDtos.SymptomAnalysisRequest();
        request.setSymptoms("bilinmeyen");
        request.setAvailableDepartments(List.of());
        
        AiDtos.SymptomAnalysisResponse mockResponse = new AiDtos.SymptomAnalysisResponse(
                "", 0, "AI servisi yanıt veremiyor.", ""
        );

        when(aiService.analyzeSymptoms(anyString(), any(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/ai/analyze-symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Controller returns 200 OK even for logical errors
                .andExpect(jsonPath("$.suggestedDepartment").value(""))
                .andExpect(jsonPath("$.confidencePercent").value(0))
                .andExpect(jsonPath("$.explanation").value("AI servisi yanıt veremiyor."));
    }
}
