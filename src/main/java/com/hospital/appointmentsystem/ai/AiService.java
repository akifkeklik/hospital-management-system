package com.hospital.appointmentsystem.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🤖 AI Semptom Analiz Servisi
 * 
 * Google Gemini API kullanarak hastaların semptomlarını analiz eder
 * ve uygun bölüm önerisinde bulunur.
 * 
 * Güvenlik:
 * - API anahtarı backend'de saklanır (frontend'e açık değil)
 * - Rate limiting: Kullanıcı başına dakikada max 5 istek
 * - Input sanitization: Max 1000 karakter
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Rate limiting: userId -> son istek zamanları
    private final ConcurrentHashMap<String, List<Long>> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    /**
     * Semptomları analiz et ve bölüm önerisi döndür.
     */
    public AiDtos.SymptomAnalysisResponse analyzeSymptoms(String symptoms, List<String> availableDepartments, String userId) {
        // 1. Validasyon
        if (symptoms == null || symptoms.trim().isEmpty()) {
            return errorResponse("Lütfen şikayetlerinizi yazın.");
        }

        if (symptoms.length() > 1000) {
            symptoms = symptoms.substring(0, 1000);
        }

        // 2. API Key kontrolü
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("demo")) {
            log.warn("Gemini API key yapılandırılmamış. Demo yanıt döndürülüyor.");
            return createDemoResponse(symptoms, availableDepartments);
        }

        // 3. Rate limiting
        if (!checkRateLimit(userId)) {
            return errorResponse("Çok fazla istek gönderdiniz. Lütfen 1 dakika bekleyin.");
        }

        // 4. Gemini API çağrısı
        try {
            String prompt = buildPrompt(symptoms, availableDepartments);
            String aiResponse = callGeminiAPI(prompt);
            return parseAiResponse(aiResponse, availableDepartments);
        } catch (Exception e) {
            log.error("AI analiz hatası: {}", e.getMessage(), e);
            return errorResponse("AI servisi şu an yanıt veremiyor. Lütfen bölümünüzü manuel seçin.");
        }
    }

    /**
     * Gemini API'ye gönderilecek prompt'u oluştur.
     * Bu prompt, AI'ın doğru formatta yanıt vermesini sağlar.
     */
    private String buildPrompt(String symptoms, List<String> departments) {
        String deptList = String.join(", ", departments);
        
        return String.format("""
            Sen bir hastane triaj asistanısın. Hastanın belirttiği semptomları analiz et ve 
            aşağıdaki mevcut bölümlerden EN UYGUN olanı öner.
            
            MEVCUT BÖLÜMLER: %s
            
            HASTANIN ŞİKAYETLERİ: %s
            
            KURALLAR:
            1. SADECE yukarıdaki mevcut bölümlerden birini önerebilirsin
            2. Güven yüzdesini 0-100 arasında ver
            3. Açıklamayı Türkçe yaz, kısa ve anlaşılır tut (max 2 cümle)
            4. Eğer semptomlar belirsizse güven yüzdesini düşük tut
            5. Yanıtını SADECE aşağıdaki JSON formatında ver, başka hiçbir şey yazma:
            
            {"department": "Bölüm Adı", "confidence": 85, "explanation": "Açıklama metni"}
            """, deptList, symptoms);
    }

    /**
     * Google Gemini API'ye HTTP isteği gönder.
     */
    private String callGeminiAPI(String prompt) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        // Gemini API request body
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", 256
            )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Gemini API hatası: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("Gemini API hatası: " + response.statusCode());
        }

        // Gemini yanıtından text'i çıkar
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            return candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }

        throw new RuntimeException("Gemini API'den geçerli yanıt alınamadı");
    }

    /**
     * AI'ın JSON yanıtını parse et.
     */
    private AiDtos.SymptomAnalysisResponse parseAiResponse(String rawResponse, List<String> availableDepartments) {
        try {
            // JSON bloğunu bul (AI bazen ekstra metin ekleyebilir)
            String jsonStr = rawResponse.trim();
            int jsonStart = jsonStr.indexOf('{');
            int jsonEnd = jsonStr.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1);
            }

            JsonNode node = objectMapper.readTree(jsonStr);
            
            String department = node.path("department").asText("");
            int confidence = node.path("confidence").asInt(50);
            String explanation = node.path("explanation").asText("Belirtileriniz değerlendirildi.");

            // Güvenlik: Önerilen bölüm gerçekten mevcut mu?
            boolean validDept = availableDepartments.stream()
                    .anyMatch(d -> d.equalsIgnoreCase(department));

            if (!validDept && !availableDepartments.isEmpty()) {
                // En yakın eşleşmeyi bul
                String closest = availableDepartments.stream()
                        .filter(d -> d.toLowerCase().contains(department.toLowerCase()) || 
                                     department.toLowerCase().contains(d.toLowerCase()))
                        .findFirst()
                        .orElse(availableDepartments.get(0));
                
                return new AiDtos.SymptomAnalysisResponse(
                        closest,
                        Math.min(confidence, 60),
                        explanation,
                        "⚠️ Bu öneri yalnızca bilgilendirme amaçlıdır. Kesin tanı için mutlaka doktorunuza danışınız."
                );
            }

            return new AiDtos.SymptomAnalysisResponse(
                    department,
                    Math.min(confidence, 95), // Max %95, kesinlik iddia etmiyoruz
                    explanation,
                    "⚠️ Bu öneri yalnızca bilgilendirme amaçlıdır. Kesin tanı için mutlaka doktorunuza danışınız."
            );
        } catch (Exception e) {
            log.error("AI yanıtı parse edilemedi: {}", rawResponse, e);
            return errorResponse("AI yanıtı işlenemedi. Lütfen bölümünüzü manuel seçin.");
        }
    }

    /**
     * API key olmadan demo yanıt (geliştirme ortamı için).
     */
    private AiDtos.SymptomAnalysisResponse createDemoResponse(String symptoms, List<String> departments) {
        String lowerSymptoms = symptoms.toLowerCase();
        
        String dept = departments.isEmpty() ? "Dahiliye" : departments.get(0);
        int confidence = 70;
        String explanation = "Belirtileriniz genel bir değerlendirme gerektirebilir.";

        if (lowerSymptoms.contains("baş ağrı") || lowerSymptoms.contains("başım ağrı") || lowerSymptoms.contains("migren") || lowerSymptoms.contains("baş dön")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("nöroloji")).findFirst().orElse(dept);
            confidence = 82;
            explanation = "Baş ağrısı ve ilişkili semptomlar nörolojik bir değerlendirme gerektirebilir.";
        } else if (lowerSymptoms.contains("göğüs") || lowerSymptoms.contains("kalp") || lowerSymptoms.contains("çarpıntı") || lowerSymptoms.contains("nefes dar")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("kardiyoloji")).findFirst().orElse(dept);
            confidence = 85;
            explanation = "Göğüs ağrısı ve kardiyovasküler semptomlar kardiyoloji değerlendirmesi gerektirir.";
        } else if (lowerSymptoms.contains("kemik") || lowerSymptoms.contains("eklem") || lowerSymptoms.contains("kırık") || lowerSymptoms.contains("bel ağrı")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("ortopedi")).findFirst().orElse(dept);
            confidence = 80;
            explanation = "Kas-iskelet sistemi şikayetleri ortopedi bölümünde değerlendirilmelidir.";
        } else if (lowerSymptoms.contains("cilt") || lowerSymptoms.contains("deri") || lowerSymptoms.contains("kaşıntı") || lowerSymptoms.contains("döküntü")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("dermatoloji")).findFirst().orElse(dept);
            confidence = 84;
            explanation = "Cilt ile ilgili şikayetler dermatoloji bölümünde değerlendirilmelidir.";
        } else if (lowerSymptoms.contains("göz") || lowerSymptoms.contains("görme") || lowerSymptoms.contains("bulanık")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("göz")).findFirst().orElse(dept);
            confidence = 83;
            explanation = "Görme ile ilgili şikayetler göz hastalıkları bölümünde değerlendirilmelidir.";
        } else if (lowerSymptoms.contains("kulak") || lowerSymptoms.contains("burun") || lowerSymptoms.contains("boğaz") || lowerSymptoms.contains("sinüzit")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("kbb") || d.toLowerCase().contains("kulak")).findFirst().orElse(dept);
            confidence = 86;
            explanation = "Kulak, burun ve boğaz şikayetleri KBB bölümünde değerlendirilmelidir.";
        } else if (lowerSymptoms.contains("mide") || lowerSymptoms.contains("karın") || lowerSymptoms.contains("ishal") || lowerSymptoms.contains("bulantı")) {
            dept = departments.stream().filter(d -> d.toLowerCase().contains("gastro") || d.toLowerCase().contains("dahiliye")).findFirst().orElse(dept);
            confidence = 79;
            explanation = "Sindirim sistemi şikayetleri gastroenteroloji veya dahiliye bölümünde değerlendirilmelidir.";
        }

        return new AiDtos.SymptomAnalysisResponse(
                dept,
                confidence,
                explanation + " (Demo mod — Gemini API key ayarlandığında gerçek AI analizi yapılacaktır.)",
                "⚠️ Bu öneri yalnızca bilgilendirme amaçlıdır. Kesin tanı için mutlaka doktorunuza danışınız."
        );
    }

    /**
     * Rate limiting kontrolü.
     */
    private boolean checkRateLimit(String userId) {
        if (userId == null) return true;
        
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60_000;

        rateLimitMap.compute(userId, (key, timestamps) -> {
            if (timestamps == null) {
                return new java.util.ArrayList<>(List.of(now));
            }
            timestamps.removeIf(t -> t < oneMinuteAgo);
            timestamps.add(now);
            return timestamps;
        });

        List<Long> timestamps = rateLimitMap.get(userId);
        return timestamps == null || timestamps.size() <= MAX_REQUESTS_PER_MINUTE;
    }

    private AiDtos.SymptomAnalysisResponse errorResponse(String message) {
        return new AiDtos.SymptomAnalysisResponse("", 0, message, "");
    }
}
