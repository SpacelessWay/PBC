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

    public void executeTransfer(String fromScoreNumber, String toScoreNumber, Long amount,Long idFrom,Long idTo) {
        // Проверяем существование счёта
        if (!scoreRepository.scoreExists(fromScoreNumber)) {
            throw new IllegalArgumentException("Счёт отправителя не найден");
        }
        if (!scoreRepository.scoreExists(toScoreNumber)) {
            throw new IllegalArgumentException("Счёт получателя не найден");
        }

        // Обновляем балансы
        jdbcTemplate.update("UPDATE scores SET balance = balance - ? WHERE score_number = ?",
                amount, fromScoreNumber);

        jdbcTemplate.update("UPDATE scores SET balance = balance + ? WHERE score_number = ?",
                amount, toScoreNumber);
        // Сгенерим UUID
        String uuid = UUID.randomUUID().toString();

        // Сохраняем историю перевода
        jdbcTemplate.update("INSERT INTO transfers (transfer_uuid, from_score_id, to_score_id, amount) VALUES (?, ?, ?, ?)",
                uuid,idFrom, idTo, amount);
    }
    /**
     * Проверяет, принадлежит ли счёт пользователю
     */
    public boolean isScoreBelongsToUser(String scoreNumber, String userUuid) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_uuid = ?",
                Integer.class, scoreNumber, userUuid);
        return count != null && count > 0;
    }
}