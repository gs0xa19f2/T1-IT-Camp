package com.example.auth_service.config;

import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataConfig implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);

            Role premiumRole = new Role();
            premiumRole.setName("ROLE_PREMIUM_USER");
            roleRepository.save(premiumRole);

            Role guestRole = new Role();
            guestRole.setName("ROLE_GUEST");
            roleRepository.save(guestRole);
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
            User admin = User.builder()
                    .login("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("password"))
                    .roles(new java.util.HashSet<>(java.util.Collections.singleton(adminRole)))
                    .build();
            userRepository.save(admin);
        }
    }
}