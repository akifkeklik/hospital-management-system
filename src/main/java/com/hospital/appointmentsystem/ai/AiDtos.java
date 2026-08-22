package com.hospital.appointmentsystem.ai;

import java.util.List;

/**
 * 🤖 AI Semptom Analizi DTO'ları
 * 
 * İstek ve yanıt modellerini içerir.
 */
public class AiDtos {

    /**
     * Frontend'den gelen istek.
     * Hastanın semptomları + sistemdeki mevcut bölüm isimleri.
     */
    public static class SymptomAnalysisRequest {
        private String symptoms;
        private List<String> availableDepartments;

        public SymptomAnalysisRequest() {}

        public String getSymptoms() { return symptoms; }
        public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
        public List<String> getAvailableDepartments() { return availableDepartments; }
        public void setAvailableDepartments(List<String> availableDepartments) { this.availableDepartments = availableDepartments; }
    }

    /**
     * AI'dan dönen analiz sonucu.
     */
    public static class SymptomAnalysisResponse {
        private String suggestedDepartment;
        private int confidencePercent;
        private String explanation;
        private String disclaimer;

        public SymptomAnalysisResponse() {}

        public SymptomAnalysisResponse(String suggestedDepartment, int confidencePercent, String explanation, String disclaimer) {
            this.suggestedDepartment = suggestedDepartment;
            this.confidencePercent = confidencePercent;
            this.explanation = explanation;
            this.disclaimer = disclaimer;
        }

        public String getSuggestedDepartment() { return suggestedDepartment; }
        public void setSuggestedDepartment(String suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }
        public int getConfidencePercent() { return confidencePercent; }
        public void setConfidencePercent(int confidencePercent) { this.confidencePercent = confidencePercent; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getDisclaimer() { return disclaimer; }
        public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
    }
}
