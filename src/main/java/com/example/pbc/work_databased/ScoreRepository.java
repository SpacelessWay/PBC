package com.example.pbc.work_databased;

import com.example.pbc.exception.BadRequestException;
import com.example.pbc.exception.NotFoundException;
import com.example.pbc.model.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public class ScoreRepository {

    private static final Logger log = LoggerFactory.getLogger(ScoreRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public ScoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Создание нового счёта
     */
    public void createScore(Score score) {
        long startTime = System.currentTimeMillis();
        log.info("Создаю новый счёт: {}", score.getScoreNumber());

        if (score == null || score.getUserId() == null || score.getUserId() <= 0) {
            log.warn("Некорректные данные при создании счёта");
            throw new BadRequestException("ID пользователя обязателен");
        }

        try {
            jdbcTemplate.update(
                    "INSERT INTO scores(user_id, score_number, balance, is_active) VALUES (?, ?, ?, ?)",
                    score.getUserId(),
                    score.getScoreNumber(),
                    score.getBalance(),
                    score.getStatus()
            );
        } catch (DataAccessException e) {
            log.error("Ошибка при создании счёта: {}", score.getScoreNumber(), e);
            throw new RuntimeException("Не удалось сохранить счёт в базе данных", e);
        }
    }

    /**
     * Получение всех активных счетов пользователя
     */
    public List<Score> getScoresByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Получаю список счетов для пользователя ID {}", userId);

        if (userId == null || userId <= 0) {
            log.warn("Неверный ID пользователя");
            throw new BadRequestException("ID пользователя обязателен");
        }

        List<Score> scores = jdbcTemplate.query(
                "SELECT * FROM scores WHERE user_id = ? AND is_active = true",
                (rs, rowNum) -> new Score(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("score_number"),
                        rs.getLong("balance"),
                        rs.getBoolean("is_active")
                ),
                userId);

        if (scores.isEmpty()) {
            log.warn("Счета не найдены для пользователя ID {}", userId);
            //throw new NotFoundException("Счета не найдены для пользователя");
        }

        return scores;
    }

    /**
     * Закрытие счёта по номеру
     */
    public void closeScore(String scoreNumber) {
        long startTime = System.currentTimeMillis();
        log.info("Закрываю счёт: {}", scoreNumber);

        if (scoreNumber == null || scoreNumber.trim().isEmpty()) {
            log.warn("Номер счёта не указан");
            throw new BadRequestException("Номер счёта обязателен");
        }

        int rowsUpdated = jdbcTemplate.update(
                "UPDATE scores SET is_active = false, closed_at = NOW() WHERE score_number = ?",
                scoreNumber);

        if (rowsUpdated == 0) {
            log.warn("Счёт {} не найден", scoreNumber);
            throw new NotFoundException("Счёт не найден");
        }

        log.debug("Счёт {} успешно закрыт", scoreNumber);
        log.debug("Метод closeScore выполнен за {} мс", System.currentTimeMillis() - startTime);
    }

    /**
     * Проверяет, существует ли счёт
     */
    public boolean scoreExists(String scoreNumber) {
        long startTime = System.currentTimeMillis();
        log.info("Проверяю существование счёта: {}", scoreNumber);

        if (scoreNumber == null || scoreNumber.trim().isEmpty()) {
            log.warn("Номер счёта не указан");
            throw new BadRequestException("Номер счёта обязателен");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ?",
                Integer.class,
                scoreNumber
        );

        boolean exists = count != null && count > 0;

        return exists;
    }

    /**
     * Поиск счёта по номеру
     */
    public Optional<Score> findByScoreNumber(String scoreNumber) {
        long startTime = System.currentTimeMillis();
        log.info("Ищу счёт по номеру: {}", scoreNumber);

        if (scoreNumber == null || scoreNumber.trim().isEmpty()) {
            log.warn("Номер счёта не указан");
            throw new BadRequestException("Номер счёта обязателен");
        }

        try {
            Score score = jdbcTemplate.queryForObject(
                    "SELECT * FROM scores WHERE score_number = ?",
                    (rs, rowNum) -> new Score(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("score_number"),
                            rs.getLong("balance"),
                            rs.getBoolean("is_active")
                    ),
                    scoreNumber);

            if (score == null) {
                log.warn("Счёт {} не найден", scoreNumber);
                return Optional.empty();
            }

            log.debug("Счёт {} найден", scoreNumber);
            return Optional.of(score);

        } catch (DataAccessException e) {
            log.error("Ошибка доступа к данным при поиске счёта: {}", scoreNumber, e);
            throw new RuntimeException("Ошибка базы данных при поиске счёта", e);
        }
    }

    /**
     * Получение ID пользователя по UUID
     */
    public Long getUserIdByUuid(String uuid) {
        long startTime = System.currentTimeMillis();
        log.info("Получаю ID пользователя по UUID");

        if (uuid == null || uuid.trim().isEmpty()) {
            log.warn("UUID не указан");
            throw new BadRequestException("UUID обязателен");
        }

        try {
            Long userId = jdbcTemplate.queryForObject(
                    "SELECT app_user_id FROM users WHERE uuid = ?", Long.class, uuid);

            if (userId == null || userId <= 0) {
                log.warn("Пользователь не найден по UUID: {}", uuid);
                throw new NotFoundException("Пользователь не найден");
            }

            log.debug("Получен ID пользователя: {}", userId);
            return userId;
        } catch (DataAccessException e) {
            log.error("Ошибка при получении ID пользователя по UUID", e);
            throw new RuntimeException("Ошибка доступа к базе данных", e);
        }
    }

    /**
     * Проверка принадлежности счёта пользователю
     */
    public boolean isScoreBelongsToUser(String scoreNumber, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Проверяю принадлежность счёта {} пользователю {}", scoreNumber, userId);

        if (scoreNumber == null || scoreNumber.trim().isEmpty()) {
            log.warn("Номер счёта не указан");
            throw new BadRequestException("Номер счёта обязателен");
        }

        if (userId == null || userId <= 0) {
            log.warn("Неверный ID пользователя");
            throw new BadRequestException("ID пользователя обязателен");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scores WHERE score_number = ? AND user_id = ?",
                Integer.class, scoreNumber, userId);

        boolean belongs = count != null && count > 0;

        if (!belongs) {
            log.warn("Счёт {} не принадлежит пользователю {}", scoreNumber, userId);
        }

        return belongs;
    }
}