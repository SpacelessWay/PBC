package com.example.pbc.work_databased;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);


    private final JdbcTemplate jdbcTemplate;

    public DatabaseManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTablesIfNotExists() {
        if (!tableExists("app_users")) {
            createAppUsersTable();
        }
        if (!tableExists("users")) {
            createUsersTable();
        }
        if (!tableExists("scores")) {
            createScoresTable();
        }
        if (!tableExists("transfers")) {
            createTransfersTable();
        }
    }

    public boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                    Integer.class,
                    tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке существования таблицы {}", tableName, e);
            throw new RuntimeException("Ошибка при работе с БД", e);
        }
    }

    private void createAppUsersTable() {
        jdbcTemplate.execute("""
            CREATE TABLE app_users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL
            )
        """);
    }

    private void createUsersTable() {
        jdbcTemplate.execute("""
            CREATE TABLE users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid CHAR(36) NOT NULL UNIQUE,
                first_name VARCHAR(100),
                last_name VARCHAR(100),
                email VARCHAR(100) UNIQUE,
                phone VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                app_user_id BIGINT NOT NULL,
                FOREIGN KEY (app_user_id) REFERENCES app_users(id)
            )
        """);
    }

    private void createScoresTable() {
        jdbcTemplate.execute("""
            CREATE TABLE scores (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                account_number VARCHAR(30) NOT NULL UNIQUE,
                user_id BIGINT NOT NULL,
                balance BIGINT NOT NULL DEFAULT 0,
                currency_code CHAR(3) NOT NULL DEFAULT 'RUB',
                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                closed_at TIMESTAMP NULL DEFAULT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);
    }

    private void createTransfersTable() {
        jdbcTemplate.execute("""
            CREATE TABLE transfers (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                transfer_uuid CHAR(36) NOT NULL UNIQUE,
                from_account_id BIGINT NOT NULL,
                to_account_id BIGINT NOT NULL,
                amount BIGINT NOT NULL,
                currency_code CHAR(3) NOT NULL DEFAULT 'RUB',
                status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL DEFAULT 'SUCCESS',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                completed_at TIMESTAMP NULL DEFAULT NULL,
                FOREIGN KEY (from_account_id) REFERENCES accounts(id),
                FOREIGN KEY (to_account_id) REFERENCES accounts(id)
            )
        """);
    }
}