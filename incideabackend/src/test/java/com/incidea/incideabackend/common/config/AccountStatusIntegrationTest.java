package com.incidea.incideabackend.common.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.incidea.incideabackend.organization.Organization;
import com.incidea.incideabackend.organization.OrganizationRepository;
import com.incidea.incideabackend.user.Role;
import com.incidea.incideabackend.user.User;
import com.incidea.incideabackend.user.UserRepository;

/**
 * Integration test against the local database (Supabase/PostgreSQL at
 * {@code localhost:54322}, schema {@code app}).
 *
 * <p>Requires Supabase to be running ({@code make supabase-start}) with default
 * local credentials.</p>
 *
 * <p>Run with:
 * <pre>
 * SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54322/postgres \
 * SPRING_DATASOURCE_USERNAME=postgres \
 * SPRING_DATASOURCE_PASSWORD=postgres \
 * ./mvnw test -Dtest=AccountStatusIntegrationTest
 * </pre>
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AccountStatusIntegrationTest {

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

    private UUID activeOrgId;
    private UUID inactiveOrgId;
    private UUID activeUserId;
    private UUID inactiveUserId;
    private UUID inactiveOrgUserId;
    private UUID activePlatformAdminId;
    private UUID inactivePlatformAdminId;

    @BeforeEach
    void setUp() {
        Organization activeOrg = new Organization();
        activeOrg.setName("Active Org Test");
        activeOrg.setActive(true);
        activeOrg = organizationRepository.save(activeOrg);
        activeOrgId = activeOrg.getId();

        Organization inactiveOrg = new Organization();
        inactiveOrg.setName("Inactive Org Test");
        inactiveOrg.setActive(false);
        inactiveOrg = organizationRepository.save(inactiveOrg);
        inactiveOrgId = inactiveOrg.getId();

        activeUserId = saveUser("11111111-1111-4111-8111-111111111111", "Active User", "active@test.local",
                true, Role.TECHNICIAN, activeOrgId);
        inactiveUserId = saveUser("22222222-2222-4222-8222-222222222222", "Inactive User", "inactive@test.local",
                false, Role.TECHNICIAN, activeOrgId);
        inactiveOrgUserId = saveUser("33333333-3333-4333-8333-333333333333", "Inactive Org User", "inactive-org@test.local",
                true, Role.TECHNICIAN, inactiveOrgId);
        activePlatformAdminId = saveUser("44444444-4444-4444-8444-444444444444", "Active PA", "pa@test.local",
                true, Role.PLATFORM_ADMIN, null);
        inactivePlatformAdminId = saveUser("55555555-5555-4555-8555-555555555555", "Inactive PA", "pa-inactive@test.local",
                false, Role.PLATFORM_ADMIN, null);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllById(java.util.List.of(
                activeUserId, inactiveUserId, inactiveOrgUserId, activePlatformAdminId, inactivePlatformAdminId));
        organizationRepository.deleteAllById(java.util.List.of(activeOrgId, inactiveOrgId));
    }

    private UUID saveUser(String uuid, String name, String email, boolean active, Role role, UUID orgId) {
        User user = new User();
        user.setId(UUID.fromString(uuid));
        user.setName(name);
        user.setEmail(email);
        user.setActive(active);
        user.setRole(role);
        user.setOrganizationId(orgId);
        return userRepository.save(user).getId();
    }

    private Jwt jwtFor(UUID sub) {
        return new Jwt(
                "token-" + sub,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", sub.toString())
        );
    }

    private void stubJwt(UUID sub) {
        when(jwtDecoder.decode("token-" + sub)).thenReturn(jwtFor(sub));
    }

    @Test
    void inactiveUser_returns403UserDeactivated() throws Exception {
        stubJwt(inactiveUserId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + inactiveUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_DEACTIVATED"));
    }

    @Test
    void inactiveOrganization_returns403OrganizationDeactivated() throws Exception {
        stubJwt(inactiveOrgUserId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + inactiveOrgUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ORGANIZATION_DEACTIVATED"));
    }

    @Test
    void platformAdmin_withInactiveOrganization_passes() throws Exception {
        stubJwt(activePlatformAdminId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + activePlatformAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activePlatformAdminId.toString()));
    }

    @Test
    void inactivePlatformAdmin_returns403UserDeactivated() throws Exception {
        stubJwt(inactivePlatformAdminId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + inactivePlatformAdminId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_DEACTIVATED"));
    }

    @Test
    void activeUser_returns200() throws Exception {
        stubJwt(activeUserId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + activeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeUserId.toString()));
    }

    @Test
    void userWithoutOnboarding_returns404() throws Exception {
        UUID unknown = UUID.randomUUID();
        stubJwt(unknown);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-" + unknown))
                .andExpect(status().isNotFound());
    }
}
