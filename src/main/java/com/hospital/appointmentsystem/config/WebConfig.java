package com.hospital.appointmentsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  🌐 WEB CONFIGURATION — CORS Ayarları                           ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 CORS (Cross-Origin Resource Sharing) NEDİR?                  ║
 * ║  Tarayıcılar güvenlik gereği, bir web sayfasının farklı bir     ║
 * ║  port veya domain'deki API'ye istek atmasını engeller.          ║
 * ║                                                                  ║
 * ║  Frontend'imiz (Next.js) localhost:3000'de çalışacak.           ║
 * ║  Backend'imiz (Spring Boot) localhost:8080'de çalışıyor.        ║
 * ║                                                                  ║
 * ║  Eğer bu ayarı yapmazsak, tarayıcı Next.js'ten gelen istekleri   ║
 * ║  "CORS Error" diyerek reddeder!                                 ║
 * ║                                                                  ║
 * ║  Bu sınıf ile Spring Boot'a diyoruz ki:                          ║
 * ║  "http://localhost:3000'den gelen her türlü (GET, POST vs.)     ║
 * ║   isteğe izin ver, güvenlidir."                                 ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Sadece /api/ ile başlayan isteklere izin ver
                .allowedOriginPatterns("*") // Tüm adreslere izin ver (Vercel dahil)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // İzin verilen HTTP metotları
                .allowedHeaders("*") // Tüm başlıklara (header) izin ver
                .allowCredentials(true); // Gerekirse çerez (cookie) vb. geçişine izin ver
    }
}
