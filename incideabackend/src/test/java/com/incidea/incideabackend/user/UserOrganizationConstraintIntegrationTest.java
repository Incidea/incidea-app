package com.incidea.incideabackend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserOrganizationConstraintIntegrationTest {

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
    private UserRepository userRepository;

    private UUID createdUserId;

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            userRepository.deleteById(createdUserId);
            createdUserId = null;
        }
    }

    @Test
    void savingNonPlatformAdminWithoutOrganization_fails() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("No Org User");
        user.setEmail("no-org-" + user.getId() + "@test.local");
        user.setActive(true);
        user.setRole(Role.TECHNICIAN);
        user.setOrganizationId(null);

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(user);
            createdUserId = user.getId();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingPlatformAdminWithoutOrganization_succeeds() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Platform Admin");
        user.setEmail("platform-admin-" + user.getId() + "@test.local");
        user.setActive(true);
        user.setRole(Role.PLATFORM_ADMIN);
        user.setOrganizationId(null);

        User saved = userRepository.saveAndFlush(user);
        createdUserId = saved.getId();

        assertThat(saved.getId()).isEqualTo(user.getId());
        assertThat(saved.getOrganizationId()).isNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }
}
