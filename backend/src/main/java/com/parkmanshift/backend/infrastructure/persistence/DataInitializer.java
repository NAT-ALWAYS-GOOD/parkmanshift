package com.parkmanshift.backend.infrastructure.persistence;

import com.parkmanshift.backend.application.port.out.UserRepositoryPort;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        ensureUserExists("employee1", "password", UserRole.EMPLOYEE);
        ensureUserExists("manager1", "password", UserRole.MANAGER);
        ensureUserExists("secretary1", "password", UserRole.SECRETARY);
    }

    private void ensureUserExists(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User(
                    UUID.randomUUID(),
                    username,
                    passwordEncoder.encode(password),
                    role
            );
            userRepository.save(user);
            System.out.println("DEBUG: Created default user: " + username);
        }
    }
}
