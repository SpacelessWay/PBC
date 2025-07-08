package com.example.pbc.res_controller;

import com.example.pbc.security.JwtUtil;
import com.example.pbc.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testRegisterSuccess() throws Exception {
        String jsonRequest = """
        {
          "login": "user",
          "password": "pass123",
          "firstName": "Иван",
          "lastName": "Иванов",
          "email": "ivan@example.com",
          "phone": "+79001234567"
        }
        """;

        doNothing().when(authService).register("user", "pass123", "Иван", "Иванов", "ivan@example.com", "+79001234567");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Регистрация успешна"));

        verify(authService, times(1)).register(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterMissingLogin() throws Exception {
        String jsonRequest = """
    {
      "password": "pass123",
      "firstName": "Иван",
      "lastName": "Иванов",
      "email": "ivan@example.com",
      "phone": "+79001234567"
    }
    """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Логин не может быть пустым"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(authService.login("user", "pass123")).thenReturn(true);
        when(authService.getUuid("user")).thenReturn("uuid-123");
        when(jwtUtil.generateToken("uuid-123")).thenReturn("token-abc");

        String jsonRequest = """
        {
          "login": "user",
          "password": "pass123"
        }
        """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("uuid-123"))
                .andExpect(jsonPath("$.token").value("token-abc"));
    }

    @Test
    void testLoginFail() throws Exception {
        when(authService.login("user", "wrong")).thenReturn(false);

        String jsonRequest = """
    {
      "login": "user",
      "password": "wrong"
    }
    """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неверный логин или пароль"));
    }
}