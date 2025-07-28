package com.example.auth_service.controller;

import com.example.auth_service.dto.UpdateUserRolesRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update-roles")
    public ResponseEntity<User> updateUserRoles(@RequestBody @Valid UpdateUserRolesRequest request) {
        User updated = userRoleService.updateUserRoles(request);
        return ResponseEntity.ok(updated);
    }
}