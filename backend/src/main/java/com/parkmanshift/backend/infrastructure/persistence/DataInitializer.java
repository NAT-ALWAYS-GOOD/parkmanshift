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
        if (userRepository.findByUsername("admin").isEmpty()) {
            String password = generateRandomPassword(12);
            String checkInCode = generateRandomCheckInCode();
            User admin = new User(
                    UUID.randomUUID(),
                    "admin",
                    "Admin ADMIN",
                    passwordEncoder.encode(password),
                    UserRole.SECRETARY,
                    checkInCode
            );
            userRepository.save(admin);

            System.out.println("###################################################");
            System.out.println("#                                                 #");
            System.out.println("#  INITIAL ADMIN CREATED                          #");
            System.out.println("#  Username: admin                                #");
            System.out.println("#  Password: " + password + "                     #");
            System.out.println("#  Check-in Code: " + checkInCode + "                   #");
            System.out.println("#  PLEASE CHANGE THIS PASSWORD AFTER FIRST LOGIN  #");
            System.out.println("#                                                 #");
            System.out.println("###################################################");
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomCheckInCode() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        return String.format("%04d", random.nextInt(10000));
    }
}
