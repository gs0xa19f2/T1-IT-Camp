package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    @NotBlank(message = "Login cannot be blank")
    private String login;

    private Set<String> roles; // Например: ["ROLE_GUEST", "ROLE_PREMIUM_USER"]
}