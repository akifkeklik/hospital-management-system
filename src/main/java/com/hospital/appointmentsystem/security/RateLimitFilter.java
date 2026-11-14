package com.hospital.appointmentsystem.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    @Value("${rate-limit.auth.capacity:10}")
    private int authCapacity;

    @Value("${rate-limit.auth.minutes:1}")
    private int authMinutes;

    /*
     * Bound the number of client buckets and expire inactive entries. A plain
     * ConcurrentHashMap retains every IP ever seen, allowing memory exhaustion
     * through requests carrying many distinct source addresses.
     */
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_CLIENTS)
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    private Bucket resolveBucket(String clientKey) {
        return buckets.get(clientKey, this::newBucket);
    }

    private Bucket newBucket(String clientKey) {
        int capacity = Math.max(1, authCapacity);
        int refillMinutes = Math.max(1, authMinutes);

        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        capacity,
                        Refill.intervally(capacity, Duration.ofMinutes(refillMinutes))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        boolean isAi = requestUri.startsWith(request.getContextPath() + "/api/ai/");
        if (!isAi && !requestUri.startsWith(request.getContextPath() + "/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // AI (Gemini) çağrıları ücretli: auth ile aynı limit, ayrı kova
        Bucket bucket = resolveBucket(getClientIP(request) + (isAi ? "_ai" : "_auth"));
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(Math.max(1, authMinutes * 60)));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"Çok fazla istek gönderdiniz. Lütfen daha sonra tekrar deneyin.\"}");
    }

    private String getClientIP(HttpServletRequest request) {
        // Forwarded headers must only be accepted from trusted reverse proxies.
        return request.getRemoteAddr();
    }
}
