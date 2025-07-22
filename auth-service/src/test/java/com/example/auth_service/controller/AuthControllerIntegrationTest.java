package com.example.auth_service.controller;

import com.example.auth_service.dto.SignInRequest;
import com.example.auth_service.dto.SignUpRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
}