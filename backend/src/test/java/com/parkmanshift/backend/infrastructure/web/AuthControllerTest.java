package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.CreateUserUseCase;
import com.parkmanshift.backend.domain.model.UserRole;
import com.parkmanshift.backend.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testRegisterEmployee() throws Exception {
        String json = "{\"username\": \"jdoe\", \"fullName\": \"John Doe\", \"password\": \"secret\", \"role\": \"EMPLOYEE\"}";

        mockMvc.perform(post("/api/auth/register-employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        verify(createUserUseCase).createUser(eq("jdoe"), eq("John Doe"), eq("secret"), eq(UserRole.EMPLOYEE));
    }
}
