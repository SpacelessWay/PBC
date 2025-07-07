package com.example.pbc.rest_controller;

import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private final JdbcTemplate jdbcTemplate;

    public Controller(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/hello")
    @Operation(summary = "Пример GET-метода", description = "Возвращает приветственное сообщение")
    public String sayHello() {
        log.info("Получен запрос на /api/hello");
        return "Здравствуйте! Это PBC API.";
    }
    @GetMapping("/test-db")
    public String testDbConnection() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            log.info("Получено количество записей из таблицы users: {}", count);
            return "Количество записей в таблице users: " + count;
        } catch (Exception e) {
            log.error("Ошибка при проверке таблицы users", e);
            throw e;
        }
    }
}