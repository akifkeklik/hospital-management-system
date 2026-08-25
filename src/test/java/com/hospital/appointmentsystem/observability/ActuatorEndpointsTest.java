package com.hospital.appointmentsystem.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActuatorEndpointsTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void healthEndpoint_ShouldBePublicAndReturnUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"status\":\"UP\""), "Health should return UP status");
    }

    @Test
    public void prometheusEndpoint_ShouldBeSecured() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);
        // We configured it to require ADMIN role, so an unauthenticated request should get 401 Unauthorized or 403 Forbidden.
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN, 
            "Prometheus endpoint should be secured");
    }

    @Test
    public void envEndpoint_ShouldNotBeExposed() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/env", String.class);
        // Should return 401/403/404 depending on how Spring Security intercepts unmapped endpoints
        assertTrue(response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN, 
            "Sensitive endpoints like env should not be exposed");
    }
}
