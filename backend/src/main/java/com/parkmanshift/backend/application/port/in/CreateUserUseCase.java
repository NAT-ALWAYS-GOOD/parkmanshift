package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;

public interface CreateUserUseCase {
    User createUser(String username, String password, UserRole role);
}
