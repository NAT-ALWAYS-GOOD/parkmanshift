package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.in.CreateUserUseCase;
import com.parkmanshift.backend.application.port.in.SearchUserUseCase;
import com.parkmanshift.backend.application.port.out.UserRepositoryPort;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

public class UserService implements CreateUserUseCase, SearchUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        User user = new User(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(password),
                role
        );

        return userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.findAllByUsernameContaining(query);
    }
}
