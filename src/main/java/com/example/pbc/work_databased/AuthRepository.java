package com.example.pbc.work_databased;

import com.example.pbc.model.Data_user;
import com.example.pbc.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class AuthRepository {
    private static final Logger log = LoggerFactory.getLogger(AuthRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {
        try {
            log.info("Сохраняю пользователя с логином: {}", user.getLogin());
            jdbcTemplate.update("INSERT INTO app_users(login, password) VALUES (?, ?)",
                    user.getLogin(), user.getPassword());
            log.info("Пользователь {} успешно сохранён", user.getLogin());
        } catch (Exception e) {
            log.error("Ошибка при сохранении пользователя: {}", user.getLogin(), e);
            throw new RuntimeException("Не удалось сохранить пользователя в БД", e);
        }
    }
    public void saveDataUser(Data_user user) {
        try {
            log.info("Сохраняю данные пользователя с uuid: {}", user.getUuid());
            // Получаем ID нового пользователя
            Long appUserId = jdbcTemplate.queryForObject(
                    "SELECT id FROM app_users WHERE login = ?",
                    Long.class,
                    user.getLogin()
            );
            jdbcTemplate.update("INSERT INTO users(uuid, first_name, last_name, email, phone, app_user_id) VALUES (?, ?, ?, ?, ?,?)",
                    user.getUuid(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), appUserId);
            log.info("Пользователь {} успешно сохранён", user.getUuid());
        } catch (Exception e) {
            log.error("Ошибка при сохранении пользователя: {}", user.getUuid(), e);
            throw new RuntimeException("Не удалось сохранить пользователя в БД", e);
        }
    }

    public Optional<User> findByLogin(String login) {
        try {
            log.info("Ищу пользователя по логину: {}", login);
            User user = jdbcTemplate.queryForObject(
                    "SELECT * FROM app_users WHERE login = ?",
                    (rs, rowNum) -> new User(
                            rs.getLong("id"),
                            rs.getString("login"),
                            rs.getString("password")
                    ),
                    login);
            if (user == null) {
                log.warn("Пользователь с логином {} не найден", login);
                return Optional.empty();
            }
            log.info("Найден пользователь: {}", login);
            return Optional.of(user);
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователя с логином: {}", login, e);
            return Optional.empty();
        }
    }
    public String getUuid(String login) {
        try {
            log.info("Ищу пользователя по логину: {}", login);

            // Получаем ID пользователя из таблицы app_users
            Long appUserId = jdbcTemplate.queryForObject(
                    "SELECT id FROM app_users WHERE login = ?",
                    Long.class,
                    login
            );

            if (appUserId == null || appUserId <= 0) {
                log.warn("Пользователь с логином {} не найден в таблице app_users", login);
                return null;
            }

            log.debug("Найден appUserId для логина {}: {}", login, appUserId);

            // Получаем UUID из таблицы users
            String uuid = jdbcTemplate.queryForObject(
                    "SELECT uuid FROM users WHERE app_user_id = ?",
                    String.class,
                    appUserId
            );

            if (uuid == null || uuid.trim().isEmpty()) {
                log.warn("UUID для пользователя с логином {} не найден", login);
                return null;
            }

            log.debug("Найден UUID для пользователя {}: {}", login, uuid);
            return uuid;

        } catch (IncorrectResultSizeDataAccessException ex) {
            log.warn("Ошибка: Найдено более одного или ни одного пользователя с логином '{}'", login, ex);
            return null;
        } catch (DataAccessException ex) {
            log.error("Ошибка доступа к данным при поиске UUID для логина '{}'", login, ex);
            return null;
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при получении UUID для логина '{}'", login, ex);
            return null;
        }
    }
}
