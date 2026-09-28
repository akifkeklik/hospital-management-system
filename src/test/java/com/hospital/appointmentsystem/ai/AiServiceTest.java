package com.hospital.appointmentsystem.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AiService}.
 *
 * <p>No real Gemini API calls are made. The Gemini HTTP client is replaced with a
 * Mockito mock via the package-private test constructor. @Value fields (apiKey, apiUrl)
 * are set with ReflectionTestUtils after construction.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Demo / fallback mode (empty / "demo" API key)</li>
 *   <li>Input validation (null, empty, oversized symptoms)</li>
 *   <li>Keyword-based department matching in demo mode</li>
 *   <li>Gemini success path: JSON parsing, confidence capping, closest-dept fallback</li>
 *   <li>Gemini failure paths: HTTP errors, malformed JSON, network exceptions</li>
 *   <li>Rate limiting logic</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiService Unit Tests")
class AiServiceTest {

    private static final String FAKE_API_KEY = "fake-test-api-key-minimum-length-ok";
    private static final String FAKE_API_URL  = "https://fake-gemini-test.invalid/generateContent";

    private HttpClient mockHttpClient;

    /** Service instance wired with a real API key (will use mockHttpClient for Gemini calls). */
    private AiService aiServiceWithKey;

    /** Service instance in demo mode (empty API key — never calls HttpClient). */
    private AiService aiServiceDemoMode;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);

        aiServiceWithKey = new AiService(mockHttpClient);
        ReflectionTestUtils.setField(aiServiceWithKey, "apiKey", FAKE_API_KEY);
        ReflectionTestUtils.setField(aiServiceWithKey, "apiUrl",  FAKE_API_URL);

        aiServiceDemoMode = new AiService(mockHttpClient);
        ReflectionTestUtils.setField(aiServiceDemoMode, "apiKey", "");
        ReflectionTestUtils.setField(aiServiceDemoMode, "apiUrl",  FAKE_API_URL);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private List<String> defaultDepartments() {
        return List.of(
                "Kardiyoloji",
                "Nöroloji",
                "Ortopedi ve Travmatoloji",
                "Göz Hastalıkları",
                "Kulak Burun Boğaz (KBB)",
                "İç Hastalıkları (Dahiliye)",
                "Genel Cerrahi",
                "Çocuk Sağlığı ve Hastalıkları");
    }

    /**
     * Wraps {@code textContent} in a Gemini-format JSON envelope.
     * Quotes inside {@code textContent} are escaped so the result is valid JSON.
     */
    private String geminiEnvelope(String textContent) {
        String escaped = textContent.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + escaped + "\"}]}}]}";
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockHttpResponse(int statusCode, String body) throws Exception {
        HttpResponse<String> resp = mock(HttpResponse.class);
        when(resp.statusCode()).thenReturn(statusCode);
        when(resp.body()).thenReturn(body);
        return resp;
    }

    // ── Input Validation ────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation: null symptoms → error response, no HTTP call")
    void analyzeSymptoms_nullSymptoms_returnsErrorWithoutHttpCall() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                null, defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
        assertThat(result.getExplanation()).isNotBlank();
        verifyNoInteractions(mockHttpClient);
    }

    @Test
    @DisplayName("Validation: empty symptoms → error response, no HTTP call")
    void analyzeSymptoms_emptySymptoms_returnsError() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
        verifyNoInteractions(mockHttpClient);
    }

    @Test
    @DisplayName("Validation: whitespace-only symptoms → error response")
    void analyzeSymptoms_whitespaceOnlySymptoms_returnsError() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "   ", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Validation: symptoms > 1000 chars are truncated, processing continues")
    void analyzeSymptoms_oversizedSymptoms_truncatedAndProcessed() {
        // Repeat a known keyword so keyword matching still fires after truncation
        String longSymptoms = "baş ağrısı ".repeat(100); // > 1000 chars
        assertThat(longSymptoms).hasSizeGreaterThan(1000);

        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                longSymptoms, defaultDepartments(), "u1");

        assertThat(result).isNotNull();
        assertThat(result.getSuggestedDepartment()).isNotEmpty(); // keyword still matched
    }

    // ── Demo Mode ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Demo mode: empty API key → demo response, no HTTP call")
    void analyzeSymptoms_emptyApiKey_returnsDemoResponse() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "baş ağrısı var", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isNotEmpty();
        assertThat(result.getExplanation()).contains("Demo mod");
        verifyNoInteractions(mockHttpClient);
    }

    @Test
    @DisplayName("Demo mode: API key 'demo' → demo response, no HTTP call")
    void analyzeSymptoms_demoApiKey_returnsDemoResponse() {
        AiService svc = new AiService(mockHttpClient);
        ReflectionTestUtils.setField(svc, "apiKey", "demo");
        ReflectionTestUtils.setField(svc, "apiUrl", FAKE_API_URL);

        AiDtos.SymptomAnalysisResponse result = svc.analyzeSymptoms(
                "mide ağrısı", defaultDepartments(), "u1");

        assertThat(result.getExplanation()).contains("Demo mod");
        verifyNoInteractions(mockHttpClient);
    }

    @Test
    @DisplayName("Demo mode: 'baş ağrısı' keyword → Nöroloji")
    void analyzeSymptoms_headacheKeyword_suggestsNeurology() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "şiddetli baş ağrısı var", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).containsIgnoringCase("nöroloji");
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: 'göğüs' keyword → Kardiyoloji")
    void analyzeSymptoms_chestKeyword_suggestsCardiology() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "göğüs ağrısı ve çarpıntı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).containsIgnoringCase("kardiyoloji");
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: 'kemik' keyword → Ortopedi")
    void analyzeSymptoms_boneKeyword_suggestsOrthopedics() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "kemik ve eklem ağrısı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).containsIgnoringCase("ortopedi");
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: 'göz' keyword → Göz Hastalıkları")
    void analyzeSymptoms_eyeKeyword_suggestsOphthalmology() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "gözlerim ağrıyor, bulanık görüyorum", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).containsIgnoringCase("göz");
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: 'kulak burun boğaz' keyword → KBB")
    void analyzeSymptoms_earKeyword_suggestsENT() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "kulağım çok ağrıyor, boğazım şiş", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).containsIgnoringCase("kulak");
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: 'mide' keyword → Dahiliye or Gastro")
    void analyzeSymptoms_stomachKeyword_suggestsGastroOrInternal() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "mide bulantısı ve karın ağrısı", defaultDepartments(), "u1");

        String dept = result.getSuggestedDepartment().toLowerCase();
        assertThat(dept).containsAnyOf("dahiliye", "gastro");
    }

    @Test
    @DisplayName("Demo mode: cilt keyword → safe fallback (no dermatoloji in list)")
    void analyzeSymptoms_skinKeyword_returnsSafeFallback() {
        // Dermatoloji is NOT in the list — service must still return something
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "cilt döküntüsü ve kaşıntı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isNotNull().isNotEmpty();
        assertThat(result.getConfidencePercent()).isPositive();
    }

    @Test
    @DisplayName("Demo mode: unknown symptoms → first department as fallback")
    void analyzeSymptoms_unknownSymptoms_returnsFallback() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "genel yorgunluk hissediyorum", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isNotEmpty();
        assertThat(result.getDisclaimer()).isNotEmpty();
    }

    @Test
    @DisplayName("Demo mode: empty department list → no crash")
    void analyzeSymptoms_emptyDepartmentList_handledGracefully() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "baş ağrısı", List.of(), "u1");

        // Must not throw NPE; result structure should still be coherent
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Demo mode: disclaimer is always present in response")
    void analyzeSymptoms_demoMode_disclaimerAlwaysPresent() {
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "nefes darlığı", defaultDepartments(), "u1");

        assertThat(result.getDisclaimer()).isNotBlank();
    }

    // ── Gemini Success Path ─────────────────────────────────────────────────

    @Test
    @DisplayName("Gemini success: valid JSON response mapped to correct DTO fields")
    void analyzeSymptoms_geminiValidResponse_mapsCorrectly() throws Exception {
        String aiText = "{\"department\": \"Kardiyoloji\", \"confidence\": 92, \"explanation\": \"Kalp ile ilgili semptomlar.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "göğüs ağrısı ve çarpıntı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isEqualTo("Kardiyoloji");
        assertThat(result.getConfidencePercent()).isEqualTo(92);
        assertThat(result.getExplanation()).isEqualTo("Kalp ile ilgili semptomlar.");
        assertThat(result.getDisclaimer()).contains("bilgilendirme amaçlıdır");
    }

    @Test
    @DisplayName("Gemini success: confidence > 95 is capped at 95")
    void analyzeSymptoms_geminiHighConfidence_cappedAt95() throws Exception {
        String aiText = "{\"department\": \"Nöroloji\", \"confidence\": 99, \"explanation\": \"Nörolojik.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isLessThanOrEqualTo(95);
    }

    @Test
    @DisplayName("Gemini success: invalid department → closest match found, confidence capped at 60")
    void analyzeSymptoms_geminiInvalidDepartment_findsClosestMatchWithLowerConfidence() throws Exception {
        // AI suggests a dept name not in the list but partially matching "Kardiyoloji"
        String aiText = "{\"department\": \"Kardiyoloji Kliniği\", \"confidence\": 88, \"explanation\": \"Kalp.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "kalp çarpıntısı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isNotEmpty();
        assertThat(result.getConfidencePercent()).isLessThanOrEqualTo(60);
    }

    @Test
    @DisplayName("Gemini success: AI wraps JSON in prose text → JSON still extracted and parsed")
    void analyzeSymptoms_geminiJsonWrappedInText_stillParsed() throws Exception {
        // Gemini sometimes adds surrounding prose
        String aiText = "İşte analiz sonucu: {\"department\": \"Nöroloji\", \"confidence\": 78, \"explanation\": \"Nörolojik.\"} Umarım yardımcı olur.";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "migren", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isEqualTo("Nöroloji");
        assertThat(result.getConfidencePercent()).isEqualTo(78);
    }

    @Test
    @DisplayName("Gemini success: missing 'confidence' field → defaults to 50")
    void analyzeSymptoms_geminiMissingConfidence_defaultsTo50() throws Exception {
        String aiText = "{\"department\": \"Kardiyoloji\", \"explanation\": \"Kalp.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "göğüs ağrısı", defaultDepartments(), "u1");

        assertThat(result.getSuggestedDepartment()).isEqualTo("Kardiyoloji");
        assertThat(result.getConfidencePercent()).isEqualTo(50); // Jackson asInt default
    }

    // ── Gemini Failure Paths ────────────────────────────────────────────────

    @Test
    @DisplayName("Gemini failure: HTTP 500 → fallback error response, no crash")
    void analyzeSymptoms_geminiHttp500_returnsErrorResponse() throws Exception {
        doReturn(mockHttpResponse(500, "Internal Server Error"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result).isNotNull();
        assertThat(result.getConfidencePercent()).isEqualTo(0);
        assertThat(result.getExplanation()).isNotBlank();
    }

    @Test
    @DisplayName("Gemini failure: HTTP 401 Unauthorized → fallback error response")
    void analyzeSymptoms_geminiHttp401_returnsErrorResponse() throws Exception {
        doReturn(mockHttpResponse(401, "Unauthorized"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "göğüs ağrısı", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gemini failure: IOException (network error) → fallback error response, no crash")
    void analyzeSymptoms_networkException_returnsErrorResponse() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new IOException("Connection refused"));

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result).isNotNull();
        assertThat(result.getConfidencePercent()).isEqualTo(0);
        assertThat(result.getExplanation()).isNotBlank();
    }

    @Test
    @DisplayName("Gemini failure: InterruptedException → fallback error response")
    void analyzeSymptoms_interrupted_returnsErrorResponse() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new InterruptedException("Interrupted"));

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gemini failure: malformed JSON in text content → fallback error response")
    void analyzeSymptoms_malformedAiJson_returnsErrorResponse() throws Exception {
        doReturn(mockHttpResponse(200, geminiEnvelope("not-valid-json-at-all")))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result).isNotNull();
        assertThat(result.getConfidencePercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gemini failure: empty candidates array → fallback error response")
    void analyzeSymptoms_emptyCandidates_returnsErrorResponse() throws Exception {
        String emptyBody = "{\"candidates\":[]}";
        doReturn(mockHttpResponse(200, emptyBody))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result.getConfidencePercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gemini failure: completely empty JSON body → no crash, fallback")
    void analyzeSymptoms_emptyJsonBody_noCrash() throws Exception {
        doReturn(mockHttpResponse(200, "{}"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), "u1");

        assertThat(result).isNotNull();
    }

    // ── Rate Limiting ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Rate limiting: 6th request within a minute is rejected")
    void analyzeSymptoms_rateLimitExceeded_returnsRateLimitError() throws Exception {
        String aiText = "{\"department\": \"Kardiyoloji\", \"confidence\": 80, \"explanation\": \"OK.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        String userId = "rl-test-" + System.nanoTime(); // fresh user per test run
        String symptoms = "göğüs ağrısı";
        List<String> depts = defaultDepartments();

        // MAX_REQUESTS_PER_MINUTE = 5 — first 5 should succeed
        for (int i = 0; i < 5; i++) {
            aiServiceWithKey.analyzeSymptoms(symptoms, depts, userId);
        }

        // 6th request must be rate-limited (confidence = 0, explanation mentions wait)
        AiDtos.SymptomAnalysisResponse result = aiServiceWithKey.analyzeSymptoms(symptoms, depts, userId);
        assertThat(result.getConfidencePercent()).isEqualTo(0);
        assertThat(result.getExplanation()).contains("istek");
    }

    @Test
    @DisplayName("Rate limiting: null userId bypasses rate-limit check")
    void analyzeSymptoms_nullUserId_notRateLimited() {
        // Should return a real (demo) response, not a rate-limit error
        AiDtos.SymptomAnalysisResponse result = aiServiceDemoMode.analyzeSymptoms(
                "baş ağrısı", defaultDepartments(), null);

        assertThat(result).isNotNull();
        assertThat(result.getExplanation()).doesNotContain("Çok fazla istek");
        verifyNoInteractions(mockHttpClient);
    }

    @Test
    @DisplayName("Rate limiting: different users are tracked independently")
    void analyzeSymptoms_differentUsers_independentRateLimits() throws Exception {
        String aiText = "{\"department\": \"Kardiyoloji\", \"confidence\": 75, \"explanation\": \"OK.\"}";
        doReturn(mockHttpResponse(200, geminiEnvelope(aiText)))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        long ts = System.nanoTime();
        String userA = "userA-" + ts;
        String userB = "userB-" + ts;
        String symptoms = "mide ağrısı";
        List<String> depts = defaultDepartments();

        // Exhaust rate limit for userA only
        for (int i = 0; i < 5; i++) {
            aiServiceWithKey.analyzeSymptoms(symptoms, depts, userA);
        }
        AiDtos.SymptomAnalysisResponse rateLimited = aiServiceWithKey.analyzeSymptoms(symptoms, depts, userA);
        assertThat(rateLimited.getConfidencePercent()).isEqualTo(0); // userA rate-limited

        // userB should still be able to make requests
        AiDtos.SymptomAnalysisResponse userBResult = aiServiceWithKey.analyzeSymptoms(symptoms, depts, userB);
        assertThat(userBResult.getSuggestedDepartment()).isNotEmpty(); // userB not limited
    }
}
