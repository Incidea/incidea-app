package com.incidea.incideabackend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.incidea.incideabackend.organization.OrganizationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserOnboardingIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> System.getenv().getOrDefault("SPRING_DATASOURCE_URL",
                        "jdbc:postgresql://localhost:54322/postgres"));
        registry.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "postgres"));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "postgres"));
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("app.cors.allowed-origins", () -> "http://localhost:3000");
    }

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private UUID createdUserId;
    private UUID createdOrgId;

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            userRepository.deleteById(createdUserId);
            createdUserId = null;
        }
        if (createdOrgId != null) {
            organizationRepository.deleteById(createdOrgId);
            createdOrgId = null;
        }
    }

    private Jwt jwtFor(UUID sub, String email) {
        return new Jwt(
                "token-" + sub,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", sub.toString(), "email", email)
        );
    }

    private void stubJwt(UUID sub, String email) {
        when(jwtDecoder.decode("token-" + sub)).thenReturn(jwtFor(sub, email));
    }

    @Test
    void onboarding_createsOrganizationAndUserAndReturnsUserDto() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "onboard-" + userId + "@test.local";
        stubJwt(userId, email);

        String body = "{\"userName\":\"Alice\",\"organizationName\":\"Acme\"}";

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .header("Authorization", "Bearer token-" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ORGANIZATION_ADMIN"))
                .andExpect(jsonPath("$.organizationId").isNotEmpty());

        createdUserId = userId;
        User saved = userRepository.findById(userId).orElseThrow();
        createdOrgId = saved.getOrganizationId();

        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.ORGANIZATION_ADMIN);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(organizationRepository.findById(createdOrgId)).isPresent();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void onboarding_whenUserAlreadyExists_returnsConflict() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "dup-" + userId + "@test.local";
        stubJwt(userId, email);

        String body = "{\"userName\":\"Bob\",\"organizationName\":\"Org\"}";

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .header("Authorization", "Bearer token-" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        createdUserId = userId;
        createdOrgId = userRepository.findById(userId).orElseThrow().getOrganizationId();

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .header("Authorization", "Bearer token-" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void onboarding_withBlankNames_returnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        stubJwt(userId, "blank@test.local");

        String body = "{\"userName\":\"\",\"organizationName\":\"Org\"}";

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .header("Authorization", "Bearer token-" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onboarding_withoutToken_returnsUnauthorized() throws Exception {
        String body = "{\"userName\":\"Alice\",\"organizationName\":\"Acme\"}";

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
