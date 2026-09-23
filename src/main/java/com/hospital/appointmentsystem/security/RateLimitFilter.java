package com.hospital.appointmentsystem.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate-limit.auth.capacity:10}")
    private int authCapacity;

    @Value("${rate-limit.auth.minutes:1}")
    private int authMinutes;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(authCapacity, Refill.intervally(authCapacity, Duration.ofMinutes(authMinutes))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // OPTIONS isteklerini rate limit dışı bırakıyoruz (CORS preflight için gerekli)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Sadece auth (login/register vb.) endpoint'leri için rate limiting uyguluyoruz.
        // Public API'lerin tamamına veya farklı endpointlere farklı limitler uygulanabilir.
        if (requestURI.startsWith("/api/auth/")) {
            String ip = getClientIP(request);
            Bucket bucket = resolveBucket(ip + "_auth");
            
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                // Standart hata formatımıza uygun bir JSON döndürüyoruz
                response.getWriter().write("{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Çok fazla istek gönderdiniz. Lütfen daha sonra tekrar deneyin.\"}");
                return;
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        // Güvenlik: Spring Boot server.forward-headers-strategy=framework 
        // ayarı sayesinde X-Forwarded-For'u güvenli şekilde işler ve 
        // request.getRemoteAddr() içine asıl IP'yi koyar. Manuel parçalamak spoofing'e yol açar.
        return request.getRemoteAddr();
    }
}
