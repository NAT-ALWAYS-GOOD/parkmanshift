package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.CreateUserUseCase;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import com.parkmanshift.backend.infrastructure.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CreateUserUseCase createUserUseCase;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, CreateUserUseCase createUserUseCase) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        
        String token = jwtUtils.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register-employee")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<User> registerEmployee(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        UserRole role = UserRole.valueOf(request.getOrDefault("role", "EMPLOYEE"));

        User user = createUserUseCase.createUser(username, password, role);
        return ResponseEntity.ok(user);
    }
}
