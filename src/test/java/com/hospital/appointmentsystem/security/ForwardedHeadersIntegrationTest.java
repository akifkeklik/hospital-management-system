package com.hospital.appointmentsystem.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "server.forward-headers-strategy=framework" // Explicitly test this strategy
})
@ActiveProfiles("test")
public class ForwardedHeadersIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testForwardedProtoHttps_ShouldReturnHsts() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-Proto", "https");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/departments", HttpMethod.GET, entity, String.class);

        // HSTS Header (Strict-Transport-Security) gelmeli
        assertTrue(response.getHeaders().containsKey("Strict-Transport-Security"), "Strict-Transport-Security header must be present for secure requests");
    }

    @Test
    public void testForwardedProtoHttp_ShouldNotReturnHsts() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-Proto", "http");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/departments", HttpMethod.GET, entity, String.class);

        // HSTS Header gelmemeli
        assertTrue(!response.getHeaders().containsKey("Strict-Transport-Security"), "Strict-Transport-Security header must NOT be present for insecure requests");
    }
}

