package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.in.ChangePasswordUseCase;
import com.parkmanshift.backend.application.port.in.CreateUserUseCase;
import com.parkmanshift.backend.application.port.in.SearchUserUseCase;
import com.parkmanshift.backend.application.port.out.UserRepositoryPort;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

public class UserService implements CreateUserUseCase, SearchUserUseCase, ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String username, String fullName, String password, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        String checkInCode = generateUniqueCheckInCode();

        User user = new User(
                UUID.randomUUID(),
                username,
                fullName,
                passwordEncoder.encode(password),
                role,
                checkInCode
        );

        return userRepository.save(user);
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String generateUniqueCheckInCode() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        String code;
        do {
            code = String.format("%04d", random.nextInt(10000));
        } while (userRepository.findByCheckInCode(code).isPresent());
        return code;
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.findAllByFullNameContaining(query);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
