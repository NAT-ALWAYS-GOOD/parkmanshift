package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.out.UserRepositoryPort;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void createUser_ShouldSucceed() {
        String username = "jdoe";
        String fullName = "John Doe";
        String password = "password";
        UserRole role = UserRole.EMPLOYEE;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByCheckInCode(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(username, fullName, password, role);

        assertNotNull(createdUser);
        assertEquals(username, createdUser.getUsername());
        assertEquals(fullName, createdUser.getFullName());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals(role, createdUser.getRole());
        assertNotNull(createdUser.getCheckInCode());
        assertEquals(4, createdUser.getCheckInCode().length());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void createUser_UsernameAlreadyExists_ShouldThrowException() {
        String username = "jdoe";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser(username, "John Doe", "password", UserRole.EMPLOYEE));
    }

    @Test
    public void changePassword_ShouldSucceed() {
        String username = "jdoe";
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        User user = new User(UUID.randomUUID(), username, "John Doe", "encodedOldPassword", UserRole.EMPLOYEE, "1234");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        userService.changePassword(username, currentPassword, newPassword);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void changePassword_InvalidCurrentPassword_ShouldThrowException() {
        String username = "jdoe";
        String currentPassword = "wrongPassword";
        User user = new User(UUID.randomUUID(), username, "John Doe", "encodedOldPassword", UserRole.EMPLOYEE, "1234");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, "encodedOldPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword(username, currentPassword, "newPassword"));
    }
}
