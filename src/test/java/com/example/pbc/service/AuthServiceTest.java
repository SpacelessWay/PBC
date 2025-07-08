package com.example.pbc.service;


import com.example.pbc.model.Data_user;
import com.example.pbc.model.User;
import com.example.pbc.work_databased.AuthRepository;
import com.example.pbc.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(authRepository, passwordEncoder);
    }

    @Test
    void register_successfully_registers_new_user() throws Exception {
        String login = "user";
        String password = "password123";
        String encodedPassword = "encoded_password";
        String uuid = "uuid-1234";

        when(authRepository.findByLogin(login)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Вызываем метод регистрации
        assertDoesNotThrow(() -> authService.register(login, password, "Иван", "Иванов", "ivan@example.com", "+79001234567"));

        // Проверяем вызовы моков
        verify(authRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(password);
        verify(authRepository, times(1)).saveDataUser(any(Data_user.class));
    }

    @Test
    void register_throws_exception_when_login_is_empty() {
        String login = "";
        String password = "pass123";

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> authService.register(login, password, "Иван", "Иванов", "ivan@example.com", "+79001234567")
        );

        assertTrue(thrown.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    void register_throws_exception_when_password_too_short() {
        String login = "user";
        String password = "short";

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> authService.register(login, password, "Иван", "Иванов", "ivan@example.com", "+79001234567")
        );

        assertTrue(thrown.getMessage().contains("Пароль слишком короткий"));
    }

    @Test
    void register_throws_exception_when_login_already_exists() {
        String login = "existing_user";
        String password = "password123";

        when(authRepository.findByLogin(login)).thenReturn(Optional.of(new User(null, login, "hash")));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> authService.register(login, password, "Иван", "Иванов", "ivan@example.com", "+79001234567")
        );

        assertTrue(thrown.getMessage().contains("уже занят"));
    }

    @Test
    void login_returns_true_for_valid_credentials() {
        String login = "user";
        String rawPassword = "password123";
        String encodedPassword = "encoded_hash";

        User user = new User(1L, login, encodedPassword);
        when(authRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = authService.login(login, rawPassword);

        assertTrue(result);
        verify(authRepository, times(1)).findByLogin(login);
    }

    @Test
    void login_returns_false_for_invalid_password() {
        String login = "user";
        String rawPassword = "wrong";
        String encodedPassword = "encoded_hash";

        User user = new User(1L, login, encodedPassword);
        when(authRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = authService.login(login, rawPassword);

        assertFalse(result);
        verify(authRepository, times(1)).findByLogin(login);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    void login_returns_false_when_user_not_found() {
        String login = "nonexistent";
        String password = "any_pass123";

        when(authRepository.findByLogin(login)).thenReturn(Optional.empty());

        boolean result = authService.login(login, password);

        assertFalse(result);
        verify(authRepository, times(1)).findByLogin(login);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}