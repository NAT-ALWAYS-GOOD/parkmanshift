package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.UserRegistrationDto;
import com.parkmanshift.backend.application.port.in.CreateUserUseCase;
import com.parkmanshift.backend.domain.model.UserRole;
import com.parkmanshift.backend.infrastructure.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final CreateUserUseCase createUserUseCase;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtUtils jwtUtils, CreateUserUseCase createUserUseCase) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtils.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/register-employee")
    public ResponseEntity<Void> registerEmployee(@RequestBody UserRegistrationDto registrationDto) {
        createUserUseCase.createUser(
                registrationDto.getUsername(),
                registrationDto.getPassword(),
                UserRole.valueOf(registrationDto.getRole().getValue())
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }
}
