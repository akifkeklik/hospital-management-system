package com.hospital.appointmentsystem.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 🤖 AI Semptom Analizi Controller
 * 
 * Hastaların semptomlarını AI ile analiz ederek
 * uygun bölüm önerisinde bulunan REST endpoint.
 * 
 * Endpoint: POST /api/ai/analyze-symptoms
 * Yetki: Giriş yapmış tüm kullanıcılar (authenticated)
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze-symptoms")
    public ResponseEntity<AiDtos.SymptomAnalysisResponse> analyzeSymptoms(
            @RequestBody AiDtos.SymptomAnalysisRequest request,
            Authentication authentication
    ) {
        String userId = authentication != null ? authentication.getName() : "anonymous";

        AiDtos.SymptomAnalysisResponse response = aiService.analyzeSymptoms(
                request.getSymptoms(),
                request.getAvailableDepartments(),
                userId
        );

        return ResponseEntity.ok(response);
    }
}
