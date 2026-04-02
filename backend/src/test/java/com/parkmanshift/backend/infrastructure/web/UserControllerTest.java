package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.ChangePasswordUseCase;
import com.parkmanshift.backend.application.port.in.SearchUserUseCase;
import com.parkmanshift.backend.domain.model.User;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.PrincipalMethodArgumentResolver;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchUserUseCase searchUserUseCase;

    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @Mock
    private Authentication mockAuthentication;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PrincipalMethodArgumentResolver())
                .build();
    }

    @Test
    public void testGetMe() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User(id, "jdoe", "John Doe", "password", UserRole.EMPLOYEE, "1234");
        when(mockAuthentication.getName()).thenReturn("jdoe");
        when(searchUserUseCase.findByUsername("jdoe")).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                .principal(mockAuthentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    public void testSearchUsers() throws Exception {
        User user = new User(UUID.randomUUID(), "jdoe", "John Doe", "password", UserRole.EMPLOYEE, "1234");
        when(searchUserUseCase.searchUsers("doe")).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users/search").param("query", "doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jdoe"))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    public void testChangePassword() throws Exception {
        when(mockAuthentication.getName()).thenReturn("jdoe");

        String json = "{\"currentPassword\": \"oldPass\", \"newPassword\": \"newPass\"}";

        mockMvc.perform(post("/api/users/change-password")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(changePasswordUseCase).changePassword(eq("jdoe"), eq("oldPass"), eq("newPass"));
    }
}
