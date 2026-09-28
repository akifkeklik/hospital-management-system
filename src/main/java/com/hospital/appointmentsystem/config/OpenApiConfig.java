package com.hospital.appointmentsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Swagger UI: <a href="http://localhost:8080/swagger-ui/index.html">/swagger-ui/index.html</a>
 * <p>OpenAPI spec: <a href="http://localhost:8080/v3/api-docs">/v3/api-docs</a>
 *
 * <p>JWT authentication in Swagger UI:
 * <ol>
 *   <li>Call {@code POST /api/auth/login} to obtain the raw JWT string.</li>
 *   <li>Click the <b>Authorize</b> button and paste the token in the {@code BearerAuth} field.</li>
 *   <li>Swagger UI will include {@code Authorization: Bearer <token>} in all subsequent requests.</li>
 * </ol>
 *
 * <p><b>Note:</b> The normal application flow uses an HttpOnly cookie for JWT transport.
 * Swagger UI uses the Authorization header as a secondary mechanism exclusively for
 * interactive API testing; it does not affect the production authentication flow.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "BearerAuth";

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI hospitalManagementOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, buildBearerSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Hospital Management System API")
                .description("""
                        Full-stack hospital management platform REST API documentation.
                        
                        ## Authentication
                        
                        This API uses **JWT Bearer authentication**.
                        
                        **To authenticate in Swagger UI:**
                        1. Call `POST /api/auth/login` with your credentials.
                        2. Copy the raw JWT token from the response (or from the `jwt` cookie value).
                        3. Click the **Authorize** button at the top of this page.
                        4. Paste the token in the `BearerAuth` field (without the `Bearer ` prefix — Swagger adds it automatically).
                        5. Click **Authorize**, then **Close**.
                        
                        All subsequent requests will include `Authorization: Bearer <token>`.
                        
                        ## Roles
                        
                        | Role    | Description                          |
                        |---------|--------------------------------------|
                        | ADMIN   | Full system access                   |
                        | DOCTOR  | Doctor-scoped operations             |
                        | PATIENT | Patient-scoped operations            |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Akif Keklik")
                        .url("https://github.com/akifkeklik"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> buildServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://hospital-management-system.onrender.com")
                .description("Production Server (Render)");

        return List.of(localServer, productionServer);
    }

    private SecurityScheme buildBearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Enter your JWT token obtained from `POST /api/auth/login`.
                        Do **not** include the `Bearer ` prefix — Swagger UI adds it automatically.
                        """);
    }
}
