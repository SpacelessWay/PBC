package com.example.pbc.service;

import com.example.pbc.model.Data_user;
import com.example.pbc.model.User;
import com.example.pbc.security.PasswordEncoder;
import com.example.pbc.work_databased.AuthRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String login, String password, String firstName, String lastName, String email, String phone) {
        try {
            log.info("Попытка регистрации пользователя: {}", login);

            if (login == null || login.trim().isEmpty()) {
                throw new IllegalArgumentException("Логин не может быть пустым");
            }
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("Пароль слишком короткий");
            }

            if (authRepository.findByLogin(login).isPresent()) {
                log.warn("Регистрация провалена: логин {} уже занят", login);
                throw new IllegalArgumentException("Логин " + login + " уже занят");
            }

            String encodedPassword = passwordEncoder.encode(password);
            authRepository.save(new User(null, login, encodedPassword));


            // Сгенерим UUID
            String uuid = UUID.randomUUID().toString();
            log.info("Сгенерирован UUID: {}", uuid);
            authRepository.saveDataUser(new Data_user(login,password,uuid, firstName,lastName,email,phone));
            log.info("Пользователь {} успешно зарегистрирован", login);

        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя {}: {}", login, e.getMessage(), e);
            throw new RuntimeException("Ошибка регистрации: " + e.getMessage(), e);
        }
    }

    public boolean login(String login, String password) {
        try {
            log.info("Попытка входа для пользователя: {}", login);

            Optional<User> userOpt = authRepository.findByLogin(login);

            if (userOpt.isEmpty()) {
                log.warn("Вход не выполнен: пользователь {} не найден", login);
                return false;
            }

            boolean isMatch = passwordEncoder.matches(password, userOpt.get().getPassword());

            if (!isMatch) {
                log.warn("Вход не выполнен: неверный пароль для пользователя {}", login);
                return false;
            }

            log.info("Пользователь {} успешно вошёл", login);
            return true;

        } catch (Exception e) {
            log.error("Ошибка при попытке входа пользователя {}: {}", login, e.getMessage(), e);
            throw new RuntimeException("Ошибка при входе: " + e.getMessage(), e);
        }
    }
    public String getUuid(String login){
        return authRepository.getUuid(login);
    }
}