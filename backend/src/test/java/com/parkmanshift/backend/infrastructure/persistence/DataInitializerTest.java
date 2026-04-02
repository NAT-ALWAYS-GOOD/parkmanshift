package com.parkmanshift.backend.infrastructure.persistence;

import com.parkmanshift.backend.application.port.out.UserRepositoryPort;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    public void init_AdminMissing_ShouldCreateAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        dataInitializer.init();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User admin = userCaptor.getValue();
        assertEquals("admin", admin.getUsername());
        assertEquals("Admin ADMIN", admin.getFullName());
        assertEquals(UserRole.SECRETARY, admin.getRole());
        assertEquals("encodedPass", admin.getPassword());
    }

    @Test
    public void init_AdminExists_ShouldDoNothing() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        dataInitializer.init();

        verify(userRepository, never()).save(any(User.class));
    }
}
