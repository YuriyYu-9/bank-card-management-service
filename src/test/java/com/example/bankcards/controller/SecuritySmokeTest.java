package com.example.bankcards.controller;

import com.example.bankcards.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.example.bankcards.security.JwtAuthFilter.class
        )
)
@Import(SecuritySmokeTest.TestSecurityConfig.class)
class SecuritySmokeTest {

    @Autowired MockMvc mvc;

    @MockBean AdminUserService adminUserService;

    @Test
    void adminUsers_unauthorized_returns401() throws Exception {
        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void adminUsers_asUser_returns403() throws Exception {
        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminUsers_asAdmin_returns200() throws Exception {
        when(adminUserService.listUsers(any()))
                .thenReturn(emptyPage());

        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    private Page<com.example.bankcards.entity.User> emptyPage() {
        return new PageImpl<>(List.of());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((req, res, e) -> res.setStatus(401))
                            .accessDeniedHandler((req, res, e) -> res.setStatus(403))
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }
}
