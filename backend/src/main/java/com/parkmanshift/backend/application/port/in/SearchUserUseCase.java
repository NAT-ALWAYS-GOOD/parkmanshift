package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.User;
import java.util.List;

public interface SearchUserUseCase {
    List<User> searchUsers(String query);
}
