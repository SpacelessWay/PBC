package com.example.pbc.work_databased;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class TransferRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ScoreRepository scoreRepository;

    public TransferRepository(JdbcTemplate jdbcTemplate, ScoreRepository scoreRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.scoreRepository = scoreRepository;
    }

    public void executeTransfer(String fromScoreNumber, String toScoreNumber, float amount) {
        // Проверяем существование счёта
        if (!scoreRepository.scoreExists(fromScoreNumber)) {
            throw new IllegalArgumentException("Счёт отправителя не найден");
        }
        if (!scoreRepository.scoreExists(toScoreNumber)) {
            throw new IllegalArgumentException("Счёт получателя не найден");
        }

        // Обновляем балансы
        jdbcTemplate.update("UPDATE scores SET balance = balance - ? WHERE sore_number = ?",
                amount, fromScoreNumber);

        jdbcTemplate.update("UPDATE scores SET balance = balance + ? WHERE score_number = ?",
                amount, toScoreNumber);
        // Сгенерим UUID
        String uuid = UUID.randomUUID().toString();

        // Сохраняем историю перевода
        jdbcTemplate.update("INSERT INTO transfers (uuid,from_score_id, to_score_id, amount) VALUES (?,?, ?, ?)",
                uuid,fromScoreNumber, toScoreNumber, amount);
    }
}