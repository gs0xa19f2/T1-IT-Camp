package com.example.auth_service.controller;

import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.dto.SignInRequest;
import com.example.auth_service.dto.SignUpRequest;
import com.example.auth_service.dto.UpdateUserRolesRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role guestRole = new Role();
        guestRole.setName("ROLE_GUEST");
        roleRepository.save(guestRole);

        Role premiumRole = new Role();
        premiumRole.setName("ROLE_PREMIUM_USER");
        roleRepository.save(premiumRole);

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        User admin = User.builder()
                .login("admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("adminpass"))
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void signUpAndSignIn_shouldSucceed() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("newuser");
        signUpRequest.setEmail("newuser@test.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setRole("ROLE_GUEST");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin("newuser");
        signInRequest.setPassword("password123");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void signUpWithPremiumRole_shouldSucceed() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("premiumuser");
        signUpRequest.setEmail("premiumuser@test.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setRole("ROLE_PREMIUM_USER");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void signUpWithAdminRole_shouldFail() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("badadmin");
        signUpRequest.setEmail("badadmin@test.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setRole("ROLE_ADMIN");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessProtectedEndpoint_shouldFail_withoutToken() throws Exception {
        mockMvc.perform(get("/api/guest/hello"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpoint_shouldSucceed_withValidToken() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin("admin");
        signInRequest.setPassword("adminpass");

        MvcResult result = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseString).get("accessToken").asText();

        mockMvc.perform(get("/api/admin/hello")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/premium/hello")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void logout_shouldInvalidateAccessAndRefreshToken() throws Exception {
        // Register and login user
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("logoutuser");
        signUpRequest.setEmail("logoutuser@test.com");
        signUpRequest.setPassword("logoutpass");
        signUpRequest.setRole("ROLE_GUEST");

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponse = signupResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(signupResponse).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(signupResponse).get("refreshToken").asText();

        // Logout (invalidate both tokens)
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // Access with old access token should be forbidden (token is blacklisted)
        mockMvc.perform(get("/api/guest/hello")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());

        // Try refreshing with old refresh token should fail
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUserRoles_shouldSucceed_forAdmin() throws Exception {
        // Create user with guest role
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("rolechangeuser");
        signUpRequest.setEmail("rolechangeuser@test.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setRole("ROLE_GUEST");
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        // Login as admin to get admin token
        SignInRequest adminSignIn = new SignInRequest();
        adminSignIn.setLogin("admin");
        adminSignIn.setPassword("adminpass");
        MvcResult adminLogin = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminSignIn)))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("accessToken").asText();

        // Update user's roles to PREMIUM_USER (as admin)
        UpdateUserRolesRequest updateRequest = new UpdateUserRolesRequest();
        updateRequest.setLogin("rolechangeuser");
        updateRequest.setRoles(Collections.singleton("ROLE_PREMIUM_USER"));

        MvcResult updateResult = mockMvc.perform(post("/api/admin/update-roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode r : node.get("roles")) {
            if ("ROLE_PREMIUM_USER".equals(r.get("name").asText())) found = true;
        }
        assertThat(found).isTrue();
    }

    @Test
    void updateUserRoles_shouldFail_forNonAdmin() throws Exception {
        // Register and login guest user
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLogin("guestuser");
        signUpRequest.setEmail("guestuser@test.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setRole("ROLE_GUEST");
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        SignInRequest guestSignIn = new SignInRequest();
        guestSignIn.setLogin("guestuser");
        guestSignIn.setPassword("password123");
        MvcResult guestLogin = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestSignIn)))
                .andExpect(status().isOk())
                .andReturn();
        String guestToken = objectMapper.readTree(guestLogin.getResponse().getContentAsString()).get("accessToken").asText();

        // Try to update another user's roles as guest (should fail)
        UpdateUserRolesRequest updateRequest = new UpdateUserRolesRequest();
        updateRequest.setLogin("admin");
        updateRequest.setRoles(Collections.singleton("ROLE_PREMIUM_USER"));

        mockMvc.perform(post("/api/admin/update-roles")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRoles_shouldFail_forUnknownRole() throws Exception {
        // Login as admin to get admin token
        SignInRequest adminSignIn = new SignInRequest();
        adminSignIn.setLogin("admin");
        adminSignIn.setPassword("adminpass");
        MvcResult adminLogin = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminSignIn)))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("accessToken").asText();

        // Try to update with unknown role
        UpdateUserRolesRequest updateRequest = new UpdateUserRolesRequest();
        updateRequest.setLogin("admin");
        updateRequest.setRoles(Collections.singleton("ROLE_UNKNOWN"));

        mockMvc.perform(post("/api/admin/update-roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}