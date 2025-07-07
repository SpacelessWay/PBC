package com.example.pbc.rest_controller;

import com.example.pbc.model.AuthResponse;
import com.example.pbc.model.Data_user;
import com.example.pbc.model.User;
import com.example.pbc.security.JwtUtil;
import com.example.pbc.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создаёт нового пользователя в системе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = String.class)))
            })
    public ResponseEntity<String> register(
            @RequestBody Data_user user) {
        String login=user.getLogin();
        String password= user.getPassword();

        log.info("Попытка регистрации: {}", login);

        if (login == null || login.trim().isEmpty()) {
            log.warn("Логин не указан");
            throw new IllegalArgumentException("Логин не может быть пустым");
        }

        if (password == null || password.length() < 6) {
            log.warn("Пароль слишком короткий");
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }

        authService.register(login, password, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone());
        return ResponseEntity.ok("Регистрация успешна");
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Аутентифицирует пользователя по логину и паролю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход"),
                    @ApiResponse(responseCode = "400", description = "Неверные учетные данные")
            })
    public ResponseEntity<AuthResponse> login(
            @RequestBody User user) {
        String login= user.getLogin();
        String password= user.getPassword();

        log.info("Попытка входа: {}", login);

        if (login == null || password == null) {
            throw new IllegalArgumentException("Логин и пароль обязательны");
        }

        boolean success = authService.login(login, password);
        if (success) {
            log.info("Вход выполнен: {}", login);
            String uuid=authService.getUuid(login);
            // Генерируем JWT
            String token = jwtUtil.generateToken(uuid);
            return ResponseEntity.ok(new AuthResponse(uuid, token));
        } else {
            log.warn("Вход провален для: {}", login);
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
    }
}