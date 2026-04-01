package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.UserDto;
import com.parkmanshift.backend.application.port.in.SearchUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SearchUserUseCase searchUserUseCase;

    public UserController(SearchUserUseCase searchUserUseCase) {
        this.searchUserUseCase = searchUserUseCase;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<UserDto> results = searchUserUseCase.searchUsers(query).stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
}
