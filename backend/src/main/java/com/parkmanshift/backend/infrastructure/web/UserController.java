package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.ChangePasswordRequest;
import com.parkmanshift.api.model.UserDto;
import com.parkmanshift.backend.application.port.in.ChangePasswordUseCase;
import com.parkmanshift.backend.application.port.in.SearchUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SearchUserUseCase searchUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public UserController(
            @org.springframework.beans.factory.annotation.Qualifier("searchUserUseCase") SearchUserUseCase searchUserUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("changePasswordUseCase") ChangePasswordUseCase changePasswordUseCase) {
        this.searchUserUseCase = searchUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(java.security.Principal principal) {
        return ResponseEntity.ok(ApiMapper.toDto(searchUserUseCase.findByUsername(principal.getName())));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<UserDto> results = searchUserUseCase.searchUsers(query).stream()
                .map(user -> ApiMapper.toDto(user, false))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request, java.security.Principal principal) {
        changePasswordUseCase.changePassword(
                principal.getName(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok().build();
    }
}
