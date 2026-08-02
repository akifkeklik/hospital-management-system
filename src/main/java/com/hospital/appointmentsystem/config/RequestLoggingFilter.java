package com.hospital.appointmentsystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Generate traceId and put into MDC
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            log.info("Incoming Request\n--> {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

            // Continue the filter chain
            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log outgoing response
            log.info("<-- {} {}\nStatus={}\nDuration={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

            // Important: Clean up MDC to prevent memory leaks and data bleeding between threads
            MDC.remove("traceId");
        }
    }
}
