package com.parkmanshift.backend.application.port.out;

import com.parkmanshift.backend.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID id);
    User save(User user);
    List<User> findAllByFullNameContaining(String query);
    Optional<User> findByCheckInCode(String code);
}
