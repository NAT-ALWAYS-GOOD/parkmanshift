package com.parkmanshift.backend.infrastructure.persistence;

import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPersistenceAdapterTest {

    @Mock
    private SpringDataUserRepository userRepository;

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    @Test
    public void testMappingToDomain() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "jdoe", "John Doe", "password", UserRole.EMPLOYEE, "1234");
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(entity));

        Optional<User> result = userPersistenceAdapter.findByUsername("jdoe");

        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals(id, user.getId());
        assertEquals("jdoe", user.getUsername());
        assertEquals("John Doe", user.getFullName());
        assertEquals("password", user.getPassword());
        assertEquals(UserRole.EMPLOYEE, user.getRole());
        assertEquals("1234", user.getCheckInCode());
    }

    @Test
    public void testSaveMappingToEntity() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "jdoe", "John Doe", "password", UserRole.EMPLOYEE, "1234");
        UserEntity entity = new UserEntity(id, "jdoe", "John Doe", "password", UserRole.EMPLOYEE, "1234");

        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);

        User saved = userPersistenceAdapter.save(user);

        assertNotNull(saved);
        assertEquals("John Doe", saved.getFullName());
        verify(userRepository).save(argThat(e -> e.getFullName().equals("John Doe")));
    }
}
